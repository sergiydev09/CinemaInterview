package com.cinema.login.data.repository

import com.cinema.login.domain.model.Credentials
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginRepositoryImplTest {

    private lateinit var repository: LoginRepositoryImpl

    @Before
    fun setup() {
        repository = LoginRepositoryImpl()
    }

    @Test
    fun `login returns token`() = runTest {
        val credentials = Credentials("user", "Password1!")

        val token = repository.login(credentials)

        assertTrue(token.isNotEmpty())
    }

    @Test
    fun `login returns token for any credentials`() = runTest {
        val credentials = Credentials("anyuser", "anypass")

        val token = repository.login(credentials)

        assertTrue(token.isNotEmpty())
    }
}
