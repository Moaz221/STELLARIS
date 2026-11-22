package com.example.myapplication.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.assistant.AssistantRepository
import com.example.myapplication.data.assistant.AssistantRepositoryImpl
import com.example.myapplication.data.assistant.FailureKind
import com.example.myapplication.data.assistant.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),   // ⬅️ بيتولّد تلقائياً — مش محتاجين id من السيرفر
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val isEmergency: Boolean = false
)

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val failureKind: FailureKind? = null,
    val conversationId: String? = null,
    val showEmergencyAlert: Boolean = false
)

class AssistantViewModel(
    private val repository: AssistantRepository = AssistantRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        // رسالة ترحيبية أولية افتراضية
        _uiState.update { current ->
            current.copy(
                messages = listOf(
                    ChatMessage(
                        text = "مرحباً بك! أنا مساعد LungCare الذكي للتعليم الصحي. كيف يمكنني مساعدتك اليوم بخصوص صحة الرئة أو التحضير لزيارة الطبيب؟",
                        isUser = false,
                        timestamp = getCurrentTime()
                    )
                )
            )
        }
    }

    fun onInputTextChanged(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun dismissEmergencyAlert() {
        _uiState.update { it.copy(showEmergencyAlert = false) }
    }

    fun retry() {
        val lastUserText = _uiState.value.messages.lastOrNull { it.isUser }?.text
        if (!lastUserText.isNullOrBlank()) {
            sendMessage(lastUserText)
        }
    }

    fun sendMessage(promptText: String? = null) {
        val textToSend = (promptText ?: _uiState.value.inputText).trim()
        if (textToSend.isBlank() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(
            text = textToSend,
            isUser = true,
            timestamp = getCurrentTime()
        )

        // 1. فحص الأمان الطبي للأعراض الطارئة
        if (MedicalSafetyGuard.containsEmergencySymptoms(textToSend)) {
            val emergencyMessage = ChatMessage(
                text = "⚠️ تنبيه طبي عاجل: الأعراض التي ذكرتها (مثل خروج دم أو صعوبة تنفس حادة) تتطلب تقييماً طبياً فورياً أو التوجه لأقرب طوارئ، ولا يجب الاعتماد على المساعد الذكي.",
                isUser = false,
                timestamp = getCurrentTime(),
                isEmergency = true
            )
            _uiState.update { current ->
                current.copy(
                    messages = current.messages + userMessage + emergencyMessage,
                    inputText = "",
                    showEmergencyAlert = true
                )
            }
            return
        }

        // 2. تحديث الشاشة برسالة المستخدم وبدء التحميل
        _uiState.update { current ->
            current.copy(
                messages = current.messages + userMessage,
                inputText = "",
                isLoading = true,
                errorMessage = null,
                failureKind = null
            )
        }

        // 3. استدعاء السيرفر الحقيقي
        viewModelScope.launch {
            when (val result = repository.sendMessage(textToSend, _uiState.value.conversationId)) {
                is NetworkResult.Success -> {
                    val aiMessage = ChatMessage(
                        // ✅ الإصلاح: شلنا result.data.id خالص — UUID بيتولّد تلقائياً فوق
                        text = result.data.response,
                        isUser = false,
                        timestamp = getCurrentTime()
                    )
                    _uiState.update { current ->
                        current.copy(
                            messages = current.messages + aiMessage,
                            conversationId = result.data.conversationId ?: current.conversationId,
                            isLoading = false
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = "Error (${result.code}): ${result.message}"
                        )
                    }
                }
                is NetworkResult.Failure -> {
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            failureKind = result.kind,
                            errorMessage = result.technicalDetail
                        )
                    }
                }
            }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }
}