package com.cinema.core.data.di

import com.cinema.core.data.session.SessionManagerImpl
import com.cinema.core.domain.session.SessionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Qualifier for the CoroutineScope used by SessionManager.
 *
 * @Retention(BINARY) means the annotation is:
 * - Stored in compiled .class files (needed for Dagger/Hilt annotation processing)
 * - NOT available at runtime via reflection (not needed, saves overhead)
 *
 * Other options:
 * - SOURCE: Discarded by compiler, only in source code
 * - RUNTIME: Available at runtime via reflection
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionScope

/**
 * Hilt module for SessionManager and related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    @Singleton
    abstract fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    companion object {
        @Provides
        @Singleton
        @SessionScope
        fun provideSessionCoroutineScope(): CoroutineScope {
            return CoroutineScope(Dispatchers.Default + SupervisorJob())
        }
    }
}
