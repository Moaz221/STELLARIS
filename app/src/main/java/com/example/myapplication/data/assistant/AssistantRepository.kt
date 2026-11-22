package com.example.myapplication.data.assistant

import com.example.myapplication.core.network.ApiClient
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

// نفس الحالات اللي بيستخدمها AssistantViewModel السابق.
enum class FailureKind {
    NO_INTERNET,
    PERMISSION,
    TIMEOUT,
    HOST_UNREACHABLE,
    TLS,
    UNKNOWN
}

sealed class NetworkResult<out T> {

    data class Success<T>(
        val data: T
    ) : NetworkResult<T>()

    data class Error(
        val message: String,
        val code: Int? = null
    ) : NetworkResult<Nothing>()

    data class Failure(
        val kind: FailureKind,
        val technicalDetail: String
    ) : NetworkResult<Nothing>()
}

// ده الموديل اللي بتستلمه الشاشة، مش شكل JSON المطلوب من السيرفر.
data class AssistantChatReply(
    val answer: String,
    val conversationId: String? = null
) {
    // للتوافق مع النسخة القديمة من الـ ViewModel اللي بتقرأ data.response.
    val response: String
        get() = answer
}

interface AssistantRepository {

    suspend fun sendMessage(
        text: String,
        conversationId: String? = null
    ): NetworkResult<AssistantChatReply>
}

class AssistantRepositoryImpl(
    private val apiService: AssistantApiService =
        ApiClient.retrofit.create(AssistantApiService::class.java)
) : AssistantRepository {

    override suspend fun sendMessage(
        text: String,
        conversationId: String?
    ): NetworkResult<AssistantChatReply> {
        val question = text.trim()

        if (question.isEmpty()) {
            return NetworkResult.Error(message = "EMPTY_QUESTION")
        }

        return try {
            // بنحتفظ بـ conversationId في توقيع الدالة للتوافق مع الشاشة.
            // ما بنبعتوش لأن عقد /chat المعروض مش موثّق فيه الحقل ده.
            val httpResponse = apiService.sendMessage(
                AssistantChatBody(question = question)
            )

            if (!httpResponse.isSuccessful) {
                val code = httpResponse.code()
                httpResponse.errorBody()?.close()

                // 500 يفضل HTTP 500، مش "مفيش إنترنت" ولا رد تجريبي.
                NetworkResult.Error(
                    message = "HTTP $code",
                    code = code
                )
            } else {
                val reply = parseReply(httpResponse.body())

                if (reply != null) {
                    NetworkResult.Success(reply)
                } else {
                    NetworkResult.Failure(
                        kind = FailureKind.UNKNOWN,
                        technicalDetail = "UNEXPECTED_CHAT_RESPONSE"
                    )
                }
            }
        } catch (e: CancellationException) {
            // لو المستخدم خرج من الشاشة، نحترم إلغاء الـ coroutine.
            throw e
        } catch (e: Exception) {
            classifyFailure(e)
        }
    }

    private fun parseReply(body: JsonElement?): AssistantChatReply? {
        // يدعم JSON object زي {"answer":"..."} أو {"response":"..."}.
        if (body is JsonObject) {
            val status = body["status"].textOrNull()
            if (status.equals("error", ignoreCase = true)) {
                return null
            }

            val answer = body["answer"].textOrNull()
                ?: body["response"].textOrNull()
                ?: return null

            return AssistantChatReply(
                answer = answer,
                conversationId = body["conversation_id"].textOrNull()
            )
        }

        // يدعم كمان JSON string لو السيرفر بيرجع "نص الرد" مباشرة.
        val answer = body.textOrNull() ?: return null
        return AssistantChatReply(answer = answer)
    }

    private fun JsonElement?.textOrNull(): String? {
        val value = this as? JsonPrimitive ?: return null
        if (!value.isString) return null
        return value.content.trim().takeIf { it.isNotEmpty() }
    }

    private fun classifyFailure(error: Exception): NetworkResult.Failure {
        val kind = when (error) {
            is SocketTimeoutException -> FailureKind.TIMEOUT
            is SSLException -> FailureKind.TLS
            is SecurityException -> FailureKind.PERMISSION

            is UnknownHostException,
            is ConnectException,
            is NoRouteToHostException -> FailureKind.HOST_UNREACHABLE

            else -> FailureKind.UNKNOWN
        }

        // لا نسجل نص السؤال أو الرد أو التوكن.
        return NetworkResult.Failure(
            kind = kind,
            technicalDetail = error.javaClass.simpleName
        )
    }
}