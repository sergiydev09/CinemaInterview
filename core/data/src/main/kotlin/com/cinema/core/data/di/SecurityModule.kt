package com.cinema.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.secureDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cinema_secure_prefs"
)

/**
 * Hilt module for security-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    private const val KEYSET_NAME = "cinema_keyset"
    private const val KEYSET_PREFS = "cinema_keyset_prefs"
    private const val MASTER_KEY_URI = "android-keystore://cinema_master_key"

    @Provides
    @Singleton
    fun provideSecureDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.secureDataStore

    @Provides
    @Singleton
    fun provideAead(
        @ApplicationContext context: Context
    ): Aead {
        AeadConfig.register()
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(com.google.crypto.tink.RegistryConfiguration.get(), Aead::class.java)
    }
}
