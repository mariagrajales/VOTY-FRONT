package com.example.votacion.core.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import com.example.votacion.core.data.AuthPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authPreferences: AuthPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        // No añadir token a las rutas de login o registro
        if (url.contains("/login") || url.contains("/register")) {
            Log.d("AuthInterceptor", "Skipping Auth header for: $url")
            return chain.proceed(originalRequest)
        }

        return try {
            // Obtenemos el token de AuthPreferences (DataStore)
            val token = runBlocking { authPreferences.tokenFlow.first() }

            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/problem+json, application/json")
                .header("Content-Type", "application/json")

            if (!token.isNullOrEmpty()) {
                Log.d("AuthInterceptor", "✓ Adding Token to: ${originalRequest.url}")
                requestBuilder.header("Authorization", "Bearer $token")
            } else {
                Log.d("AuthInterceptor", "⚠ No token found in AuthPreferences")
            }

            val response = chain.proceed(requestBuilder.build())

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
