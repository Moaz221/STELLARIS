package com.example.myapplication.data.local.chat

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

data class StoredConversation(
    val conversation: ConversationEntity,
    val messages: List<MessageEntity>
)

/**
 * كل حاجة بتتحفظ على الجهاز بس (Room). مفيش أي حاجة بتتبعت لـ Firebase.
 */
class ChatHistoryRepository(private val database: ChatDatabase) {

    private val dao = database.chatDao()

    fun observeConversations(): Flow<List<ConversationSummary>> = dao.observeSummaries()

    suspend fun loadConversation(id: String): StoredConversation? {
        val conversation = dao.getConversation(id) ?: return null
        return StoredConversation(conversation, dao.getMessages(id))
    }

    /**
     * بيحفظ رسايل في محادثة. لو المحادثة لسه مش موجودة وفيه [titleHint] (يعني أول رسالة
     * مستخدم) بيتعمل لها صف جديد. لو مش موجودة ومفيش [titleHint] (مثلاً رد AI وصل بعد ما
     * المحادثة اتمسحت) مبنكتبش حاجة عشان المحادثة المحذوفة ما ترجعش.
     */
    suspend fun appendMessages(
        conversationId: String,
        messages: List<MessageEntity>,
        titleHint: String? = null,
        serverConversationId: String? = null
    ) {
        if (messages.isEmpty()) return

        database.withTransaction {
            val latest = messages.maxOf { it.timestamp }

            if (dao.getConversation(conversationId) == null) {
                if (titleHint == null) return@withTransaction
                dao.insertConversation(
                    ConversationEntity(
                        id = conversationId,
                        title = buildTitle(titleHint),
                        serverConversationId = serverConversationId,
                        createdAt = messages.minOf { it.timestamp },
                        updatedAt = latest
                    )
                )
            }

            dao.insertMessages(messages)
            dao.touch(conversationId, latest)
            if (serverConversationId != null) {
                dao.setServerConversationId(conversationId, serverConversationId)
            }
        }
    }

    suspend fun deleteConversation(id: String) = dao.deleteConversation(id)

    suspend fun deleteAll() = dao.deleteAll()

    private fun buildTitle(firstUserText: String): String {
        val cleaned = firstUserText.trim().replace(Regex("\\s+"), " ")
        if (cleaned.length <= MAX_TITLE_LENGTH) return cleaned

        var cut = cleaned.take(MAX_TITLE_LENGTH)
        // ما نقطعش في نص emoji (surrogate pair)
        if (cut.isNotEmpty() && cut.last().isHighSurrogate()) cut = cut.dropLast(1)
        return cut.trimEnd() + "…"
    }

    private companion object {
        const val MAX_TITLE_LENGTH = 40
    }
}
