package com.example.myapplication.data.assistant

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatQuestionRequest(
    @SerialName("question") val question: String
)

@Serializable
data class ChatSource(
    @SerialName("uid") val uid: String? = null,
    @SerialName("snippet") val snippet: String? = null
)

@Serializable
data class ChatApiResponse(
    @SerialName("status") val status: String? = null,
    @SerialName("question") val question: String? = null,
    @SerialName("answer") val answer: String,
    @SerialName("sources") val sources: List<ChatSource> = emptyList()
)

@Serializable
data class ChatMessageRequest(
    @SerialName("message") val message: String,
    @SerialName("conversation_id") val conversationId: String? = null
)

@Serializable
data class ChatMessageResponse(
    @SerialName("id") val id: String? = null,
    @SerialName("response") val response: String,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("disclaimer") val disclaimer: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
