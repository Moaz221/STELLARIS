package com.example.myapplication.core.network

import okhttp3.Interceptor
import okhttp3.Response

class LocalTunnelInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val modifiedRequest = originalRequest.newBuilder()
            // هذا الـ Header يجعل LocalTunnel يمرر طلبات الـ API مباشرة للـ FastAPI
            .header("bypass-tunnel-reminder", "true")
            .header("User-Agent", "LungCare-Android-Client/1.0")
            .header("Accept", "application/json")
            .build()
        return chain.proceed(modifiedRequest)
    }
}
