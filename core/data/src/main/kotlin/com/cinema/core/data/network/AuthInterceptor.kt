package com.cinema.core.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that adds the Bearer token to all API requests.
 * The token is set by the SessionManager after successful login.
 */
@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    @Volatile
    private var token: String? = null

    /**
     * Sets the authentication token to be used in requests.
     * @param token The Bearer token for API authentication
     */
    fun setToken(token: String?) {
        this.token = token
    }

    /**
     * Clears the authentication token.
     */
    fun clearToken() {
        token = null
    }

    /**
     * Checks if a token is currently set.
     * @return true if a token is available
     */
    fun hasToken(): Boolean = token != null

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // If no token is set, proceed with the original request
        val currentToken = token ?: return chain.proceed(originalRequest)

        // Add the Authorization header with Bearer token
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $currentToken")
            .header("accept", "application/json")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
