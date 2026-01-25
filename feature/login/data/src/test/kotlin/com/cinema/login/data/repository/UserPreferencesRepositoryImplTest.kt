package com.cinema.login.data.repository

import com.cinema.core.data.datasource.SecureLocalDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class UserPreferencesRepositoryImplTest {

    private lateinit var secureLocalDataSource: SecureLocalDataSource
    private lateinit var repository: UserPreferencesRepositoryImpl

    @Before
    fun setup() {
        secureLocalDataSource = mockk(relaxed = true)
        repository = UserPreferencesRepositoryImpl(secureLocalDataSource)
    }

    @Test
    fun `saveUsername saves username to secure storage`() = runTest {
        val username = "testuser"
        val valueSlot = slot<String>()
        val typeSlot = slot<KType>()

        repository.saveUsername(username)

        coVerify { secureLocalDataSource.save("saved_username", capture(valueSlot), capture(typeSlot)) }
        assertEquals(username, valueSlot.captured)
        assertEquals(typeOf<String>(), typeSlot.captured)
    }

    @Test
    fun `getSavedUsername returns username from secure storage`() = runTest {
        val expectedUsername = "saveduser"
        coEvery { secureLocalDataSource.get<Any>("saved_username", ofType<KType>()) } returns expectedUsername

        val result = repository.getSavedUsername()

        assertEquals(expectedUsername, result)
    }

    @Test
    fun `getSavedUsername returns null when no username saved`() = runTest {
        coEvery { secureLocalDataSource.get<Any>("saved_username", ofType<KType>()) } returns null

        val result = repository.getSavedUsername()

        assertNull(result)
    }

    @Test
    fun `clearUsername removes username from secure storage`() = runTest {
        repository.clearUsername()

        coVerify { secureLocalDataSource.remove("saved_username") }
    }
}
