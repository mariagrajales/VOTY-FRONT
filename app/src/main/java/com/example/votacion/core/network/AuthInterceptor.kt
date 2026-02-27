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
        return try {
            // Get token synchronously from EncryptedSharedPreferences
            val token = tokenManager.getToken()
            
            val originalRequest = chain.request()
            Log.d("AuthInterceptor", "=== REQUEST ===")
            Log.d("AuthInterceptor", "Method: ${originalRequest.method}")
            Log.d("AuthInterceptor", "URL: ${originalRequest.url}")
            Log.d("AuthInterceptor", "Headers: ${originalRequest.headers}")
            
            // Build request with required headers
            // some endpoints (DELETE/PUT) expect application/problem+json in Accept
            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/problem+json, application/json")
                .header("Content-Type", "application/json")
            
            // Add Authorization header if token exists
            if (token.isNotEmpty()) {
                Log.d("AuthInterceptor", "✓ Token found (length: ${token.length}), adding Authorization header")
                requestBuilder.header("Authorization", "Bearer $token")
            } else {
                Log.d("AuthInterceptor", "✗ No token found, proceeding without Authorization header")
            }
            
            val request = requestBuilder.build()

            val response = chain.proceed(request)
            Log.d("AuthInterceptor", "=== RESPONSE ===")
            Log.d("AuthInterceptor", "Code: ${response.code}")
            Log.d("AuthInterceptor", "Message: ${response.message}")
            Log.d("AuthInterceptor", "URL: ${response.request.url}")
            
            if (response.code >= 400) {
                try {
                    val body = response.peekBody(Long.MAX_VALUE).string()
                    Log.d("AuthInterceptor", "Response Body: $body")
                } catch (e: Exception) {
                    Log.d("AuthInterceptor", "Could not read response body")
                }
            }
            
            response
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Error in AuthInterceptor", e)
            throw e
        }
    }
}
