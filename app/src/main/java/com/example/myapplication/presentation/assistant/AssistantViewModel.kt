package com.example.myapplication.presentation.assistant

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.myapplication.data.assistant.AssistantRepository
import com.example.myapplication.data.assistant.AssistantRepositoryImpl
import com.example.myapplication.data.assistant.FailureKind
import com.example.myapplication.data.assistant.NetworkResult
import com.example.myapplication.data.local.chat.ChatDatabase
import com.example.myapplication.data.local.chat.ChatHistoryRepository
import com.example.myapplication.data.local.chat.MessageEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    /** فاضي لو [isEmergency]: الواجهة بتعرض نص الطوارئ المترجم من strings.xml. */
    val text: String,
    val isUser: Boolean,
    /** epoch millis. التنسيق بيتم في الـ UI حسب لغة التطبيق. */
    val timestamp: Long,
    val isEmergency: Boolean = false
)

/** أخطاء بدل الـ strings الجاهزة، والـ UI هو اللي بيترجمها. */
sealed interface ChatError {
    data class Server(val code: String, val message: String) : ChatError
    data class Network(val kind: FailureKind, val technicalDetail: String?) : ChatError

    /** آخر رسالة في المحادثة المحفوظة مالهاش رد (التطبيق اتقفل أو المحادثة اتقفلت قبل الرد). */
    data object Interrupted : ChatError
}

data class AssistantUiState(
    val conversationId: String = UUID.randomUUID().toString(),
    val serverConversationId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isRestoring: Boolean = false,
    val error: ChatError? = null,
    val showEmergencyAlert: Boolean = false
)

class AssistantViewModel(
    private val history: ChatHistoryRepository,
    private val assistantRepository: AssistantRepository = AssistantRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    /** طلب شغّال لكل محادثة. الطلب بيكمل ويتحفظ حتى لو المستخدم فتح محادثة تانية. */
    private val requests = ConcurrentHashMap<String, Job>()
    private var loadJob: Job? = null

    // ───────────────────────── الإرسال ─────────────────────────

    fun sendMessage(text: String) {
        val clean = text.trim()
        val state = _uiState.value
        if (clean.isEmpty() || state.isLoading || state.isRestoring) return

        val conversationId = state.conversationId
        val userMessage = newMessage(text = clean, isUser = true)

        // فحص الطوارئ في مكان واحد، ومبنبعتش للسيرفر (زي السلوك الأصلي)
        if (MedicalSafetyGuard.containsEmergencySymptoms(clean)) {
            val emergency = newMessage(
                text = "",
                isUser = false,
                isEmergency = true,
                timestamp = userMessage.timestamp + 1
            )
            _uiState.update {
                it.copy(
                    messages = it.messages + userMessage + emergency,
                    error = null,
                    showEmergencyAlert = true
                )
            }
            persist(conversationId, listOf(userMessage, emergency))
            return
        }

        _uiState.update {
            it.copy(messages = it.messages + userMessage, isLoading = true, error = null)
        }
        persist(conversationId, listOf(userMessage))
        requestReply(conversationId, clean, state.serverConversationId)
    }

    /**
     * بيعيد الطلب لآخر رسالة مستخدم من غير ما يضيفها تاني في القائمة
     * (الـ retry القديم كان بيكرر الرسالة).
     */
    fun retry() {
        val state = _uiState.value
        if (state.isLoading || state.isRestoring) return
        val lastUserMessage = state.messages.lastOrNull()?.takeIf { it.isUser } ?: return

        _uiState.update { it.copy(isLoading = true, error = null) }
        requestReply(state.conversationId, lastUserMessage.text, state.serverConversationId)
    }

    private fun requestReply(
        conversationId: String,
        prompt: String,
        serverConversationId: String?
    ) {
        val job = viewModelScope.launch {
            var reply: ChatMessage? = null
            var newServerId: String? = null
            var error: ChatError? = null

            try {
                when (val result = assistantRepository.sendMessage(prompt, serverConversationId)) {
                    is NetworkResult.Success -> {
                        reply = newMessage(text = result.data.response, isUser = false)
                        newServerId = result.data.conversationId
                    }

                    is NetworkResult.Error -> {
                        error = ChatError.Server(
                            code = result.code.toString(),
                            message = result.message.toString()
                        )
                    }

                    is NetworkResult.Failure -> {
                        error = ChatError.Network(result.kind, result.technicalDetail)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Assistant request failed", e)
                error = ChatError.Network(FailureKind.UNKNOWN, e.message)
            }

            ensureActive()

            val aiMessage = reply
            val serverId = newServerId
            val failure = error

            // بنحفظ الرد في المحادثة اللي طلبته حتى لو المستخدم انتقل لغيرها
            if (aiMessage != null) {
                persist(conversationId, listOf(aiMessage), serverConversationId = serverId)
            }

            // وبنحدّث الشاشة بس لو لسه على نفس المحادثة
            _uiState.update { current ->
                if (current.conversationId != conversationId) {
                    current
                } else {
                    current.copy(
                        messages = if (aiMessage != null) current.messages + aiMessage else current.messages,
                        serverConversationId = serverId ?: current.serverConversationId,
                        isLoading = false,
                        error = failure
                    )
                }
            }
        }

        requests[conversationId] = job
        job.invokeOnCompletion { requests.remove(conversationId) }
    }

    // ───────────────────────── المحادثات ─────────────────────────

    fun startNewConversation() {
        loadJob?.cancel()
        _uiState.value = AssistantUiState()
    }

    fun openConversation(id: String) {
        if (_uiState.value.conversationId == id) return

        loadJob?.cancel()
        _uiState.value = AssistantUiState(conversationId = id, isRestoring = true)

        loadJob = viewModelScope.launch {
            val stored = try {
                history.loadConversation(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load conversation $id", e)
                null
            }

            if (stored == null) {
                _uiState.value = AssistantUiState()
                return@launch
            }

            val messages = stored.messages.map { it.toChatMessage() }
            val waitingForReply = requests.containsKey(id)
            val unanswered = !waitingForReply && messages.lastOrNull()?.isUser == true

            _uiState.update { current ->
                if (current.conversationId != id) {
                    current
                } else {
                    current.copy(
                        serverConversationId = stored.conversation.serverConversationId,
                        messages = messages,
                        isRestoring = false,
                        isLoading = waitingForReply,
                        error = if (unanswered) ChatError.Interrupted else null
                    )
                }
            }
        }
    }

    /** بتتندهي بعد ما شاشة الـ History تمسح محادثة. */
    fun onConversationDeleted(id: String) {
        requests.remove(id)?.cancel()
        if (_uiState.value.conversationId == id) startNewConversation()
    }

    fun onAllConversationsDeleted() {
        requests.values.forEach { it.cancel() }
        requests.clear()
        startNewConversation()
    }

    fun dismissEmergencyAlert() {
        _uiState.update { it.copy(showEmergencyAlert = false) }
    }

    // ───────────────────────── التخزين ─────────────────────────

    private fun persist(
        conversationId: String,
        messages: List<ChatMessage>,
        serverConversationId: String? = null
    ) {
        val titleHint = messages.firstOrNull { it.isUser }?.text
        val entities = messages.map { it.toEntity(conversationId) }

        viewModelScope.launch {
            try {
                // NonCancellable عشان الكتابة ما تتقطعش لو الـ ViewModel اتقفل في نصها
                withContext(NonCancellable) {
                    history.appendMessages(conversationId, entities, titleHint, serverConversationId)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save messages", e)
            }
        }
    }

    private fun newMessage(
        text: String,
        isUser: Boolean,
        isEmergency: Boolean = false,
        timestamp: Long = System.currentTimeMillis()
    ) = ChatMessage(
        text = text,
        isUser = isUser,
        timestamp = timestamp,
        isEmergency = isEmergency
    )

    companion object {
        private const val TAG = "AssistantViewModel"

        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return viewModelFactory {
                initializer {
                    AssistantViewModel(ChatHistoryRepository(ChatDatabase.get(appContext)))
                }
            }
        }
    }
}

private fun ChatMessage.toEntity(conversationId: String) = MessageEntity(
    id = id,
    conversationId = conversationId,
    text = text,
    isUser = isUser,
    timestamp = timestamp,
    isEmergency = isEmergency
)

private fun MessageEntity.toChatMessage() = ChatMessage(
    id = id,
    text = text,
    isUser = isUser,
    timestamp = timestamp,
    isEmergency = isEmergency
)