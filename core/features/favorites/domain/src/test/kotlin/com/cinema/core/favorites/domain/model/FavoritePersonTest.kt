package com.cinema.core.favorites.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FavoritePersonTest {

    private val person = FavoritePerson(
        id = 1,
        name = "Test Person",
        profileUrl = "https://example.com/profile.jpg"
    )

    @Test
    fun `equals returns true for same data`() {
        val other = FavoritePerson(
            id = 1,
            name = "Test Person",
            profileUrl = "https://example.com/profile.jpg"
        )
        assertEquals(person, other)
    }

    @Test
    fun `equals returns false for different id`() {
        val other = person.copy(id = 2)
        assertNotEquals(person, other)
    }

    @Test
    fun `hashCode is consistent for equal objects`() {
        val other = FavoritePerson(
            id = 1,
            name = "Test Person",
            profileUrl = "https://example.com/profile.jpg"
        )
        assertEquals(person.hashCode(), other.hashCode())
    }

    @Test
    fun `toString contains all properties`() {
        val result = person.toString()
        assert(result.contains("1"))
        assert(result.contains("Test Person"))
        assert(result.contains("https://example.com/profile.jpg"))
    }

    @Test
    fun `copy creates new instance with updated values`() {
        val copied = person.copy(name = "New Name")
        assertEquals("New Name", copied.name)
        assertEquals(person.id, copied.id)
        assertEquals(person.profileUrl, copied.profileUrl)
    }

    @Test
    fun `component functions return correct values`() {
        val (id, name, profileUrl) = person
        assertEquals(1, id)
        assertEquals("Test Person", name)
        assertEquals("https://example.com/profile.jpg", profileUrl)
    }

    @Test
    fun `handles null profileUrl`() {
        val personWithNull = FavoritePerson(
            id = 1,
            name = "Test",
            profileUrl = null
        )
        assertEquals(null, personWithNull.profileUrl)
    }
}
