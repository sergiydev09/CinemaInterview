package com.cinema.core.data.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var interceptor: AuthInterceptor
    private lateinit var chain: Interceptor.Chain
    private lateinit var response: Response

    @Before
    fun setup() {
        interceptor = AuthInterceptor()
        chain = mockk()
        response = mockk()
    }

    @Test
    fun `hasToken returns false initially`() {
        assertFalse(interceptor.hasToken())
    }

    @Test
    fun `hasToken returns true after setToken`() {
        interceptor.setToken("token123")

        assertTrue(interceptor.hasToken())
    }

    @Test
    fun `hasToken returns false after clearToken`() {
        interceptor.setToken("token123")
        interceptor.clearToken()

        assertFalse(interceptor.hasToken())
    }

    @Test
    fun `intercept adds Authorization header when token is set`() {
        val originalRequest = Request.Builder()
            .url("https://api.example.com/test")
            .build()
        val requestSlot = slot<Request>()

        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } returns response

        interceptor.setToken("myToken")
        interceptor.intercept(chain)

        val capturedRequest = requestSlot.captured
        assertEquals("Bearer myToken", capturedRequest.header("Authorization"))
        assertEquals("application/json", capturedRequest.header("accept"))
    }

    @Test
    fun `intercept proceeds without Authorization header when no token`() {
        val originalRequest = Request.Builder()
            .url("https://api.example.com/test")
            .build()
        val requestSlot = slot<Request>()

        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } returns response

        interceptor.intercept(chain)

        val capturedRequest = requestSlot.captured
        assertEquals(null, capturedRequest.header("Authorization"))
    }

    @Test
    fun `intercept returns response from chain`() {
        val originalRequest = Request.Builder()
            .url("https://api.example.com/test")
            .build()

        every { chain.request() } returns originalRequest
        every { chain.proceed(any()) } returns response

        val result = interceptor.intercept(chain)

        assertEquals(response, result)
    }

    @Test
    fun `setToken with null clears the token`() {
        interceptor.setToken("token")
        interceptor.setToken(null)

        assertFalse(interceptor.hasToken())
    }
}
