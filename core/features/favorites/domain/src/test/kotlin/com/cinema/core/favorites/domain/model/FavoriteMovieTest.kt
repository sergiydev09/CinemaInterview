package com.cinema.core.favorites.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FavoriteMovieTest {

    private val movie = FavoriteMovie(
        id = 1,
        title = "Test Movie",
        posterUrl = "https://example.com/poster.jpg",
        releaseDate = "2024-01-01"
    )

    @Test
    fun `equals returns true for same data`() {
        val other = FavoriteMovie(
            id = 1,
            title = "Test Movie",
            posterUrl = "https://example.com/poster.jpg",
            releaseDate = "2024-01-01"
        )
        assertEquals(movie, other)
    }

    @Test
    fun `equals returns false for different id`() {
        val other = movie.copy(id = 2)
        assertNotEquals(movie, other)
    }

    @Test
    fun `hashCode is consistent for equal objects`() {
        val other = FavoriteMovie(
            id = 1,
            title = "Test Movie",
            posterUrl = "https://example.com/poster.jpg",
            releaseDate = "2024-01-01"
        )
        assertEquals(movie.hashCode(), other.hashCode())
    }

    @Test
    fun `toString contains all properties`() {
        val result = movie.toString()
        assert(result.contains("1"))
        assert(result.contains("Test Movie"))
        assert(result.contains("https://example.com/poster.jpg"))
        assert(result.contains("2024-01-01"))
    }

    @Test
    fun `copy creates new instance with updated values`() {
        val copied = movie.copy(title = "New Title")
        assertEquals("New Title", copied.title)
        assertEquals(movie.id, copied.id)
        assertEquals(movie.posterUrl, copied.posterUrl)
        assertEquals(movie.releaseDate, copied.releaseDate)
    }

    @Test
    fun `component functions return correct values`() {
        val (id, title, posterUrl, releaseDate) = movie
        assertEquals(1, id)
        assertEquals("Test Movie", title)
        assertEquals("https://example.com/poster.jpg", posterUrl)
        assertEquals("2024-01-01", releaseDate)
    }

    @Test
    fun `handles null posterUrl and releaseDate`() {
        val movieWithNulls = FavoriteMovie(
            id = 1,
            title = "Test",
            posterUrl = null,
            releaseDate = null
        )
        assertEquals(null, movieWithNulls.posterUrl)
        assertEquals(null, movieWithNulls.releaseDate)
    }
}
