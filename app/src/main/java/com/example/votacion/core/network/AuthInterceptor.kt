package com.example.votacion.core.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import com.example.votacion.core.data.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // ESCAPE: Si es login o register, no añadimos Authorization
        if (url.contains("/login") || url.contains("/register")) {
            Log.d("AuthInterceptor", "Skipping Auth header for: $url")
            return chain.proceed(originalRequest)
        }

        return try {
            val token = tokenManager.getToken()
            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/problem+json, application/json")
                .header("Content-Type", "application/json")

            // Solo añadimos el token si realmente existe y no es login/register
            if (!token.isNullOrEmpty()) {
                Log.d("AuthInterceptor", "✓ Adding Token to: ${originalRequest.url}")
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val response = chain.proceed(requestBuilder.build())

            // Log de errores para depuración
            if (response.code >= 400) {
                val body = response.peekBody(Long.MAX_VALUE).string()
                Log.d("AuthInterceptor", "Error ${response.code} en ${response.request.url}: $body")
            }

            response
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error in AuthInterceptor", e)
            throw e
        }
    }
}
