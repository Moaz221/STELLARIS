package com.example.myapplication.data.local.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    /** IGNORE (مش REPLACE) عشان REPLACE بيمسح الرسايل بسبب الـ CASCADE. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversation(id: String): ConversationEntity?

    @Query("SELECT * FROM messages WHERE conversationId = :id ORDER BY timestamp ASC, rowid ASC")
    suspend fun getMessages(id: String): List<MessageEntity>

    @Query(
        """
        SELECT
            c.id AS id,
            c.title AS title,
            c.updatedAt AS updatedAt,
            (SELECT COUNT(*) FROM messages m WHERE m.conversationId = c.id) AS messageCount,
            (SELECT m.text FROM messages m
                WHERE m.conversationId = c.id AND m.text != ''
                ORDER BY m.timestamp DESC, m.rowid DESC LIMIT 1) AS lastMessage
        FROM conversations c
        ORDER BY c.updatedAt DESC
        """
    )
    fun observeSummaries(): Flow<List<ConversationSummary>>

    @Query("UPDATE conversations SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun touch(id: String, updatedAt: Long)

    @Query("UPDATE conversations SET serverConversationId = :serverId WHERE id = :id")
    suspend fun setServerConversationId(id: String, serverId: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("DELETE FROM conversations")
    suspend fun deleteAll()
}
