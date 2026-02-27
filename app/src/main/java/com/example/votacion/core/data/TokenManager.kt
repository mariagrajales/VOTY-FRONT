package com.example.votacion.core.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "token_preferences",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        android.util.Log.d("TokenManager", "Saving token: ${token.take(20)}... (length: ${token.length})")
        sharedPreferences.edit().putString("auth_token", token).apply()
        android.util.Log.d("TokenManager", "Token saved successfully")
    }
    
    fun getToken(): String {
        val token = sharedPreferences.getString("auth_token", "") ?: ""
        android.util.Log.d("TokenManager", "Retrieved token: ${if (token.isEmpty()) "EMPTY" else token.take(20)}... (length: ${token.length})")
        return token
    }
    
    fun clearToken() {
        android.util.Log.d("TokenManager", "Clearing token")
        sharedPreferences.edit().remove("auth_token").apply()
        android.util.Log.d("TokenManager", "Token cleared")
    }
}
