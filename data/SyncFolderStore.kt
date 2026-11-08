package com.graphene.antivirus.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SyncFolderStore(private val context: Context) {

    private val KEY_FOLDERS = stringPreferencesKey("sync_folders_json")
    private val gson = Gson()
    private val listType = object : TypeToken<List<SyncFolder>>() {}.type

    val foldersFlow: Flow<List<SyncFolder>> = context.dataStore.data.map { prefs ->
        decode(prefs[KEY_FOLDERS])
    }

    suspend fun getFolders(): List<SyncFolder> =
        decode(context.dataStore.data.first()[KEY_FOLDERS])

    suspend fun addFolder(uri: String, name: String) {
        val current = getFolders().toMutableList()
        if (current.none { it.uri == uri }) {
            current.add(SyncFolder(uri = uri, name = name))
            persist(current)
        }
    }

    suspend fun removeFolder(uri: String) {
        val current = getFolders().filterNot { it.uri == uri }
        persist(current)
    }

    private suspend fun persist(folders: List<SyncFolder>) {
        val json = gson.toJson(folders)
        context.dataStore.edit { it[KEY_FOLDERS] = json }
    }

    private fun decode(raw: String?): List<SyncFolder> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            gson.fromJson<List<SyncFolder>>(raw, listType) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}