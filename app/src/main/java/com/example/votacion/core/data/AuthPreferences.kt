package com.example.votacion.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "auth_preferences")

object PreferencesKey {
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val USER_EMAIL = stringPreferencesKey("user_email")
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_ID = stringPreferencesKey("user_id")
}

@Singleton
class AuthPreferences @Inject constructor(@ApplicationContext private val context: Context) {
    
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKey.AUTH_TOKEN] = token
        }
    }

    suspend fun saveUser(userId: String, userEmail: String, userName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKey.USER_ID] = userId
            preferences[PreferencesKey.USER_EMAIL] = userEmail
            preferences[PreferencesKey.USER_NAME] = userName
        }
    }

    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun getToken(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKey.AUTH_TOKEN] ?: ""
    }

    fun getUserEmail(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKey.USER_EMAIL] ?: ""
    }

    fun getUserName(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKey.USER_NAME] ?: ""
    }

    fun getUserId(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKey.USER_ID] ?: ""
    }
}
