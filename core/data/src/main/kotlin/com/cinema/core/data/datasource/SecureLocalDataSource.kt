package com.cinema.core.data.datasource

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@Singleton
class SecureLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val aead: Aead,
    private val json: Json
) {

    suspend fun <T> save(key: String, value: T, type: KType) {
        val serializer = json.serializersModule.serializer(type)
        val jsonString = json.encodeToString(serializer, value)
        val encrypted = aead.encrypt(jsonString.toByteArray(Charsets.UTF_8), null)
        val encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = encoded
        }
    }

    suspend fun <T> get(key: String, type: KType): T? {
        val prefKey = stringPreferencesKey(key)
        val encoded = dataStore.data.map { preferences ->
            preferences[prefKey]
        }.first() ?: return null

        return try {
            val encrypted = Base64.decode(encoded, Base64.DEFAULT)
            val decrypted = aead.decrypt(encrypted, null)
            val jsonString = String(decrypted, Charsets.UTF_8)
            val serializer = json.serializersModule.serializer(type)
            @Suppress("UNCHECKED_CAST")
            json.decodeFromString(serializer, jsonString) as T
        } catch (_: Exception) {
            remove(key)
            null
        }
    }

    suspend fun remove(key: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences.remove(prefKey)
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T> SecureLocalDataSource.save(key: String, value: T) {
    save(key, value, typeOf<T>())
}

@OptIn(InternalSerializationApi::class)
suspend inline fun <reified T> SecureLocalDataSource.get(key: String): T? {
    return get(key, typeOf<T>())
}
