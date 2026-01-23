package com.cinema.core.data.datasource

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.crypto.tink.Aead
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecureLocalDataSourceTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var aead: Aead
    private lateinit var moshi: Moshi
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
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        setupAeadMock()
        setupDataStoreMock()

        secureLocalDataSource = SecureLocalDataSource(dataStore, aead, moshi)
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
                "removed" // Return non-null to satisfy type
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
        secureLocalDataSource.save("test_key", "test_value", String::class.java)

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

        secureLocalDataSource.save("key", "hello", String::class.java)

        val json = String(capturedData.captured, Charsets.UTF_8)
        assertEquals("\"hello\"", json)
    }

    @Test
    fun `save calls datastore updateData`() = runTest {
        secureLocalDataSource.save("key", "value", String::class.java)

        coVerify { dataStore.updateData(any()) }
    }

    @Test
    fun `get returns decrypted value from datastore`() = runTest {
        // First save a value
        secureLocalDataSource.save("my_key", "my_value", String::class.java)

        // Then get it back
        val result = secureLocalDataSource.get("my_key", String::class.java)

        assertEquals("my_value", result)
    }

    @Test
    fun `get returns null when key does not exist`() = runTest {
        val result = secureLocalDataSource.get("nonexistent", String::class.java)

        assertNull(result)
    }

    @Test
    fun `get calls aead decrypt`() = runTest {
        secureLocalDataSource.save("key", "value", String::class.java)

        val capturedData = slot<ByteArray>()
        every { aead.decrypt(capture(capturedData), any()) } answers {
            val data = firstArg<ByteArray>()
            data.copyOfRange("ENCRYPTED:".length, data.size)
        }

        secureLocalDataSource.get("key", String::class.java)

        assertTrue(capturedData.isCaptured)
        assertTrue(String(capturedData.captured).startsWith("ENCRYPTED:"))
    }

    @Test
    fun `get returns null and removes key on decryption failure`() = runTest {
        secureLocalDataSource.save("bad_key", "value", String::class.java)
        val key = stringPreferencesKey("bad_key")
        assertTrue(inMemoryPreferences.containsKey(key))

        every { aead.decrypt(any(), any()) } throws RuntimeException("Decryption failed")

        val result = secureLocalDataSource.get("bad_key", String::class.java)

        assertNull(result)
        assertTrue(!inMemoryPreferences.containsKey(key))
    }

    @Test
    fun `get returns null on json parsing failure`() = runTest {
        // Store invalid JSON that will fail to parse as TestData
        val invalidJson = "not valid json for TestData"
        val encrypted = "ENCRYPTED:".toByteArray() + invalidJson.toByteArray()
        val encoded = java.util.Base64.getEncoder().encodeToString(encrypted)
        val key = stringPreferencesKey("invalid_key")
        inMemoryPreferences[key] = encoded

        val result = secureLocalDataSource.get("invalid_key", TestData::class.java)

        assertNull(result)
    }

    @Test
    fun `remove deletes key from datastore`() = runTest {
        secureLocalDataSource.save("to_delete", "value", String::class.java)
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
        secureLocalDataSource.save("key1", "value1", String::class.java)
        secureLocalDataSource.save("key2", "value2", String::class.java)
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

        secureLocalDataSource.save("user", testData, TestData::class.java)
        val retrieved = secureLocalDataSource.get("user", TestData::class.java)

        assertEquals("Alice", retrieved?.name)
        assertEquals(30, retrieved?.age)
        assertEquals(listOf("tag1", "tag2"), retrieved?.tags)
    }

    @Test
    fun `save and get works with integer type`() = runTest {
        secureLocalDataSource.save("count", 42, Int::class.java)
        val result = secureLocalDataSource.get("count", Int::class.java)

        assertEquals(42, result)
    }

    @Test
    fun `save and get works with boolean type`() = runTest {
        secureLocalDataSource.save("flag", true, Boolean::class.java)
        val result = secureLocalDataSource.get("flag", Boolean::class.java)

        assertEquals(true, result)
    }

    @Test
    fun `overwriting key updates value`() = runTest {
        secureLocalDataSource.save("key", "first", String::class.java)
        secureLocalDataSource.save("key", "second", String::class.java)

        val result = secureLocalDataSource.get("key", String::class.java)

        assertEquals("second", result)
    }

    @Test
    fun `multiple keys can be stored independently`() = runTest {
        secureLocalDataSource.save("key1", "value1", String::class.java)
        secureLocalDataSource.save("key2", "value2", String::class.java)

        assertEquals("value1", secureLocalDataSource.get("key1", String::class.java))
        assertEquals("value2", secureLocalDataSource.get("key2", String::class.java))
    }

    // Tests for reified extension functions

    @Test
    fun `reified save extension works with string`() = runTest {
        secureLocalDataSource.save("reified_key", "reified_value")

        val result = secureLocalDataSource.get("reified_key", String::class.java)
        assertEquals("reified_value", result)
    }

    @Test
    fun `reified get extension works with string`() = runTest {
        secureLocalDataSource.save("key", "value", String::class.java)

        val result: String? = secureLocalDataSource.get("key")
        assertEquals("value", result)
    }

    @Test
    fun `reified save and get extensions work together`() = runTest {
        secureLocalDataSource.save("combined_key", "combined_value")

        val result: String? = secureLocalDataSource.get("combined_key")
        assertEquals("combined_value", result)
    }

    @Test
    fun `reified extensions work with data class`() = runTest {
        val testData = TestData("Bob", 25, listOf("developer"))

        secureLocalDataSource.save("user_reified", testData)
        val result: TestData? = secureLocalDataSource.get("user_reified")

        assertEquals("Bob", result?.name)
        assertEquals(25, result?.age)
        assertEquals(listOf("developer"), result?.tags)
    }

    @Test
    fun `reified get extension returns null for missing key`() = runTest {
        val result: String? = secureLocalDataSource.get("nonexistent_reified")

        assertNull(result)
    }

    @Test
    fun `reified extensions work with integer`() = runTest {
        secureLocalDataSource.save("int_key", 123)

        val result: Int? = secureLocalDataSource.get("int_key")
        assertEquals(123, result)
    }

    @Test
    fun `reified extensions work with boolean`() = runTest {
        secureLocalDataSource.save("bool_key", false)

        val result: Boolean? = secureLocalDataSource.get("bool_key")
        assertEquals(false, result)
    }

    @Test
    fun `reified extensions work with list`() = runTest {
        val list = listOf("a", "b", "c")

        @Suppress("UNCHECKED_CAST")
        secureLocalDataSource.save("list_key", list, List::class.java as Class<List<String>>)

        @Suppress("UNCHECKED_CAST")
        val result = secureLocalDataSource.get("list_key", List::class.java as Class<List<String>>)
        assertEquals(listOf("a", "b", "c"), result)
    }

    data class TestData(val name: String, val age: Int, val tags: List<String> = emptyList())
}
