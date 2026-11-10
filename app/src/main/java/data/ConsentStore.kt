package com.graphene.antivirus.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class ConsentStore(private val context: Context) {
    private val KEY_CONSENT       = booleanPreferencesKey("consent_granted")
    private val KEY_DEVICE_ID     = stringPreferencesKey("device_id")
    private val KEY_SYNC_ENABLED  = booleanPreferencesKey("sync_enabled")
    private val KEY_API_TOKEN     = stringPreferencesKey("api_token")
    private val KEY_REGISTERED_AT = stringPreferencesKey("registered_at")

    val consentFlow: Flow<Boolean>      = context.dataStore.data.map { it[KEY_CONSENT] ?: false }
    val syncEnabledFlow: Flow<Boolean>  = context.dataStore.data.map { it[KEY_SYNC_ENABLED] ?: false }
    val apiTokenFlow: Flow<String?>     = context.dataStore.data.map { it[KEY_API_TOKEN] }

    suspend fun setConsent(granted: Boolean) {
        context.dataStore.edit { it[KEY_CONSENT] = granted }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SYNC_ENABLED] = enabled }
    }

    suspend fun syncEnabled(): Boolean =
        context.dataStore.data.first()[KEY_SYNC_ENABLED] ?: false

    suspend fun getOrCreateDeviceId(): String {
        var id: String? = null
        context.dataStore.edit { prefs ->
            id = prefs[KEY_DEVICE_ID] ?: UUID.randomUUID().toString().also { prefs[KEY_DEVICE_ID] = it }
        }
        return id!!
    }

    suspend fun getApiToken(): String? =
        context.dataStore.data.first()[KEY_API_TOKEN]

    suspend fun saveApiToken(token: String) {
        context.dataStore.edit {
            it[KEY_API_TOKEN] = token
            it[KEY_REGISTERED_AT] = java.time.Instant.now().toString()
        }
    }

    suspend fun clearApiToken() {
        context.dataStore.edit { it.remove(KEY_API_TOKEN) }
    }
}