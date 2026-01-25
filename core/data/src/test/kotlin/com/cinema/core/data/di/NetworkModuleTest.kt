package com.cinema.core.data.di

import com.cinema.core.data.network.AuthInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.serialization.Serializable

class NetworkModuleTest {

    private lateinit var authInterceptor: AuthInterceptor

    @Before
    fun setup() {
        authInterceptor = AuthInterceptor()
        authInterceptor.setToken("test_token")
    }

    @Test
    fun `provideJson returns configured Json instance`() {
        val json = NetworkModule.provideJson()

        assertNotNull(json)
        // Verify it can serialize/deserialize Kotlin classes
        val serialized = json.encodeToString(TestData.serializer(), TestData("test", 42))
        val parsed = json.decodeFromString(TestData.serializer(), serialized)

        assertEquals("test", parsed.name)
        assertEquals(42, parsed.value)
    }

    @Test
    fun `provideLoggingInterceptor returns interceptor with BODY level`() {
        val interceptor = NetworkModule.provideLoggingInterceptor()

        assertEquals(HttpLoggingInterceptor.Level.BODY, interceptor.level)
    }

    @Test
    fun `provideOkHttpClient has correct timeout configuration`() {
        val loggingInterceptor = NetworkModule.provideLoggingInterceptor()
        val okHttpClient = NetworkModule.provideOkHttpClient(authInterceptor, loggingInterceptor)

        assertEquals(30, okHttpClient.connectTimeoutMillis / 1000)
        assertEquals(30, okHttpClient.readTimeoutMillis / 1000)
        assertEquals(30, okHttpClient.writeTimeoutMillis / 1000)
    }

    @Test
    fun `provideOkHttpClient has auth interceptor`() {
        val loggingInterceptor = NetworkModule.provideLoggingInterceptor()
        val okHttpClient = NetworkModule.provideOkHttpClient(authInterceptor, loggingInterceptor)

        assertTrue(okHttpClient.interceptors.any { it is AuthInterceptor })
    }

    @Test
    fun `provideOkHttpClient has logging interceptor`() {
        val loggingInterceptor = NetworkModule.provideLoggingInterceptor()
        val okHttpClient = NetworkModule.provideOkHttpClient(authInterceptor, loggingInterceptor)

        assertTrue(okHttpClient.interceptors.any { it is HttpLoggingInterceptor })
    }

    @Test
    fun `provideRetrofit has correct base URL`() {
        val json = NetworkModule.provideJson()
        val loggingInterceptor = NetworkModule.provideLoggingInterceptor()
        val okHttpClient = NetworkModule.provideOkHttpClient(authInterceptor, loggingInterceptor)

        val retrofit = NetworkModule.provideRetrofit(okHttpClient, json)

        assertEquals("https://api.themoviedb.org/3/", retrofit.baseUrl().toString())
    }

    @Test
    fun `provideRetrofit uses provided OkHttpClient`() {
        val json = NetworkModule.provideJson()
        val loggingInterceptor = NetworkModule.provideLoggingInterceptor()
        val okHttpClient = NetworkModule.provideOkHttpClient(authInterceptor, loggingInterceptor)

        val retrofit = NetworkModule.provideRetrofit(okHttpClient, json)

        assertEquals(okHttpClient, retrofit.callFactory())
    }

    @Test
    fun `provideRetrofit has KotlinxSerializationConverterFactory`() {
        val json = NetworkModule.provideJson()
        val loggingInterceptor = NetworkModule.provideLoggingInterceptor()
        val okHttpClient = NetworkModule.provideOkHttpClient(authInterceptor, loggingInterceptor)

        val retrofit = NetworkModule.provideRetrofit(okHttpClient, json)

        val converterFactories = retrofit.converterFactories()
        // The KotlinxSerializationConverterFactory is an anonymous class, check by package name
        assertTrue(
            converterFactories.any {
                it.javaClass.name.contains("kotlinx.serialization") ||
                it.javaClass.name.contains("KotlinxSerialization") ||
                it.javaClass.name.contains("asConverterFactory")
            }
        )
    }

    @Serializable
    data class TestData(val name: String, val value: Int)
}
