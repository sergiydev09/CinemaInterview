package com.cinema.core.data.datasource

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecureLocalDataSourceTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var aead: Aead
    private lateinit var json: Json
    private lateinit var secureLocalDataSource: SecureLocalDataSource

    private val inMemoryPreferences = mutableMapOf<Preferences.Key<*>, Any>()

    @Before
    fun setup() {
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg())
        }
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }

        dataStore = mockk()
        aead = mockk()
        json = Json { ignoreUnknownKeys = true }

        setupAeadMock()
        setupDataStoreMock()

        secureLocalDataSource = SecureLocalDataSource(dataStore, aead, json)
    }

    private fun setupAeadMock() {
        every { aead.encrypt(any(), any()) } answers {
            "ENCRYPTED:".toByteArray() + firstArg<ByteArray>()
        }
        every { aead.decrypt(any(), any()) } answers {
            val data = firstArg<ByteArray>()
            data.copyOfRange("ENCRYPTED:".length, data.size)
        }
    }

    private fun setupDataStoreMock() {
        coEvery { dataStore.updateData(any()) } coAnswers {
            val transform = firstArg<suspend (Preferences) -> Preferences>()
            val mutablePrefs = mockk<MutablePreferences>()
            val keySlot = slot<Preferences.Key<Any>>()
            val valueSlot = slot<Any>()

            every { mutablePrefs.set(capture(keySlot), capture(valueSlot)) } answers {
                inMemoryPreferences[keySlot.captured] = valueSlot.captured
            }
            @Suppress("UNCHECKED_CAST")
            every { mutablePrefs.remove(any<Preferences.Key<String>>()) } answers {
                val key = firstArg<Preferences.Key<String>>()
                inMemoryPreferences.remove(key)
                "removed"
            }
            every { mutablePrefs.clear() } answers {
                inMemoryPreferences.clear()
            }
            every { mutablePrefs.toPreferences() } returns mockk()
            every { mutablePrefs.toMutablePreferences() } returns mutablePrefs

            transform(mutablePrefs)
        }

        every { dataStore.data } answers {
            val prefs = mockk<Preferences>()
            every { prefs[any<Preferences.Key<String>>()] } answers {
                val key = firstArg<Preferences.Key<String>>()
                inMemoryPreferences[key] as? String
            }
            flowOf(prefs)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
        inMemoryPreferences.clear()
    }

    @Test
    fun `save encrypts value and stores in datastore`() = runTest {
        secureLocalDataSource.save("test_key", "test_value")

        val key = stringPreferencesKey("test_key")
        assertTrue(inMemoryPreferences.containsKey(key))

        val storedValue = inMemoryPreferences[key] as String
        assertTrue(storedValue.isNotEmpty())
    }

    @Test
    fun `save calls aead encrypt with json serialized data`() = runTest {
        val capturedData = slot<ByteArray>()
        every { aead.encrypt(capture(capturedData), any()) } answers {
            "ENCRYPTED:".toByteArray() + firstArg<ByteArray>()
        }

        secureLocalDataSource.save("key", "hello")

        val jsonString = String(capturedData.captured, Charsets.UTF_8)
        assertEquals("\"hello\"", jsonString)
    }

    @Test
    fun `save calls datastore updateData`() = runTest {
        secureLocalDataSource.save("key", "value")

        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `get returns decrypted value from datastore`() = runTest {
        secureLocalDataSource.save("my_key", "my_value")

        val result: String? = secureLocalDataSource.get("my_key")

        assertEquals("my_value", result)
    }

    @Test
    fun `get returns null when key does not exist`() = runTest {
        val result: String? = secureLocalDataSource.get("nonexistent")

        assertNull(result)
    }

    @Test
    fun `get calls aead decrypt`() = runTest {
        secureLocalDataSource.save("key", "value")

        val capturedData = slot<ByteArray>()
        every { aead.decrypt(capture(capturedData), any()) } answers {
            val data = firstArg<ByteArray>()
            data.copyOfRange("ENCRYPTED:".length, data.size)
        }

        secureLocalDataSource.get<String>("key")

        assertTrue(capturedData.isCaptured)
        assertTrue(String(capturedData.captured).startsWith("ENCRYPTED:"))
    }

    @Test
    fun `get returns null and removes key on decryption failure`() = runTest {
        secureLocalDataSource.save("bad_key", "value")
        val key = stringPreferencesKey("bad_key")
        assertTrue(inMemoryPreferences.containsKey(key))

        every { aead.decrypt(any(), any()) } throws RuntimeException("Decryption failed")

        val result: String? = secureLocalDataSource.get("bad_key")

        assertNull(result)
        assertTrue(!inMemoryPreferences.containsKey(key))
    }

    @Test
    fun `remove deletes key from datastore`() = runTest {
        secureLocalDataSource.save("to_delete", "value")
        val key = stringPreferencesKey("to_delete")
        assertTrue(inMemoryPreferences.containsKey(key))

        secureLocalDataSource.remove("to_delete")

        assertTrue(!inMemoryPreferences.containsKey(key))
    }

    @Test
    fun `remove calls datastore updateData`() = runTest {
        secureLocalDataSource.remove("any_key")

        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `clear removes all keys from datastore`() = runTest {
        secureLocalDataSource.save("key1", "value1")
        secureLocalDataSource.save("key2", "value2")
        assertEquals(2, inMemoryPreferences.size)

        secureLocalDataSource.clear()

        assertTrue(inMemoryPreferences.isEmpty())
    }

    @Test
    fun `clear calls datastore updateData`() = runTest {
        secureLocalDataSource.clear()

        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `save and get works with complex data class`() = runTest {
        val testData = TestData("Alice", 30, listOf("tag1", "tag2"))

        secureLocalDataSource.save("user", testData)
        val retrieved: TestData? = secureLocalDataSource.get("user")

        assertEquals("Alice", retrieved?.name)
        assertEquals(30, retrieved?.age)
        assertEquals(listOf("tag1", "tag2"), retrieved?.tags)
    }

    @Test
    fun `save and get works with integer type`() = runTest {
        secureLocalDataSource.save("count", 42)
        val result: Int? = secureLocalDataSource.get("count")

        assertEquals(42, result)
    }

    @Test
    fun `save and get works with boolean type`() = runTest {
        secureLocalDataSource.save("flag", true)
        val result: Boolean? = secureLocalDataSource.get("flag")

        assertEquals(true, result)
    }

    @Test
    fun `overwriting key updates value`() = runTest {
        secureLocalDataSource.save("key", "first")
        secureLocalDataSource.save("key", "second")

        val result: String? = secureLocalDataSource.get("key")

        assertEquals("second", result)
    }

    @Test
    fun `multiple keys can be stored independently`() = runTest {
        secureLocalDataSource.save("key1", "value1")
        secureLocalDataSource.save("key2", "value2")

        assertEquals("value1", secureLocalDataSource.get<String>("key1"))
        assertEquals("value2", secureLocalDataSource.get<String>("key2"))
    }

    @Serializable
    data class TestData(val name: String, val age: Int, val tags: List<String> = emptyList())
}
