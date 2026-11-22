package com.example.myapplication.data.assistant

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class AssistantChatBody(
    val question: String,
    val message: String? = null
)

interface AssistantApiService {

    @POST("chat")
    suspend fun sendMessage(
        @Body body: AssistantChatBody
    ): Response<JsonElement>

    @GET("health")
    suspend fun health(): Response<JsonElement>
}