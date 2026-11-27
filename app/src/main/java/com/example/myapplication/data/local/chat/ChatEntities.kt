package com.example.myapplication.data.local.chat

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * محادثة واحدة. الـ id بيتولّد في الـ ViewModel (UUID) فبنقدر نحفظ الرسايل
 * قبل ما نستنى أي عملية على الداتابيز.
 */
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    /** الـ id اللي الباك اند رجّعه، عشان نكمل نفس المحادثة بعد ما نفتحها من الـ History. */
    val serverConversationId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    /** فاضي لو الرسالة طوارئ (النص بيتعرض من strings.xml عشان يتترجم). */
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val isEmergency: Boolean
)

/** اللي بتعرضه شاشة الـ History لكل محادثة. */
data class ConversationSummary(
    val id: String,
    val title: String,
    val updatedAt: Long,
    val messageCount: Int,
    val lastMessage: String?
)
