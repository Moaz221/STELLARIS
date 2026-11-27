package com.example.myapplication.data.vision

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VisionApiService {
    @Multipart
    @POST("predict")
    suspend fun predictXray(@Part file: MultipartBody.Part): Response<PredictResponse>
}
