package com.cinema.core.data.datasource

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataSource for secure encrypted storage using Tink and DataStore.
 * Handles encryption/decryption and storage of sensitive data using AES-GCM.
 * Supports any serializable type through Moshi.
 */
@Singleton
class SecureLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val aead: Aead,
    private val moshi: Moshi
) {

    suspend fun <T> save(key: String, value: T, clazz: Class<T>) {
        val json = moshi.adapter(clazz).toJson(value)
        val encrypted = aead.encrypt(json.toByteArray(Charsets.UTF_8), null)
        val encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { preferences ->
            preferences[prefKey] = encoded
        }
    }

    suspend fun <T> get(key: String, clazz: Class<T>): T? {
        val prefKey = stringPreferencesKey(key)
        val encoded = dataStore.data.map { preferences ->
            preferences[prefKey]
        }.first() ?: return null

        return try {
            val encrypted = Base64.decode(encoded, Base64.DEFAULT)
            val decrypted = aead.decrypt(encrypted, null)
            val json = String(decrypted, Charsets.UTF_8)
            moshi.adapter(clazz).fromJson(json)
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

suspend inline fun <reified T> SecureLocalDataSource.save(key: String, value: T) {
    save(key, value, T::class.java)
}

suspend inline fun <reified T> SecureLocalDataSource.get(key: String): T? {
    return get(key, T::class.java)
}
