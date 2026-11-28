package com.example.myapplication.data.vision

import android.content.Context
import android.net.Uri
import com.example.myapplication.core.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed class VisionResult {
    data class Success(val data: PredictResponse) : VisionResult()
    data class Error(val message: String, val code: Int? = null) : VisionResult()
    data class Exception(val throwable: Throwable) : VisionResult()
}

interface VisionRepository {
    suspend fun analyzeXray(context: Context, imageUri: Uri): VisionResult
}

class VisionRepositoryImpl(
    private val api: VisionApiService = ApiClient.retrofit.create(VisionApiService::class.java),
) : VisionRepository {

    override suspend fun analyzeXray(context: Context, imageUri: Uri): VisionResult = withContext(Dispatchers.IO) {
        try {
            // 1) Decode, downscale (max 1600 px) and re-encode as PNG
            val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(imageUri)?.use { android.graphics.BitmapFactory.decodeStream(it, null, opts) }
            var sample = 1
            while (opts.outWidth / sample > 1600 || opts.outHeight / sample > 1600) sample *= 2
            val bmp = context.contentResolver.openInputStream(imageUri)?.use {
                android.graphics.BitmapFactory.decodeStream(it, null, android.graphics.BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return@withContext VisionResult.Error("Cannot open image")
            val tempFile = File(context.cacheDir, "xray_${UUID.randomUUID()}.png")
            FileOutputStream(tempFile).use { out -> bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
            bmp.recycle()

            // 2) Validate size (max 10MB)
            if (tempFile.length() > (10 * 1024 * 1024)) {
                tempFile.delete()
                return@withContext VisionResult.Error("Image too large (max 10MB)")
            }

            // 3) Multipart body
            val requestFile = tempFile.asRequestBody("image/png".toMediaType())
            val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

            // 4) Call API
            val response = api.predictXray(body)
            tempFile.delete() // cleanup

            if (response.isSuccessful && (response.body() != null)) {
                VisionResult.Success(response.body()!!)
            } else {
                VisionResult.Error(
                    response.errorBody()?.string() ?: "Server error",
                    response.code(),
                )
            }
        } catch (e: Exception) {
            VisionResult.Exception(e)
        }
    }
}
