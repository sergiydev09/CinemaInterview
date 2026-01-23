package com.cinema.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImageUrlBuilderTest {

    @Test
    fun `buildUrl with valid path returns full URL with default size`() {
        val result = ImageUrlBuilder.buildUrl("/abc123.jpg")

        assertEquals("https://image.tmdb.org/t/p/w500/abc123.jpg", result)
    }

    @Test
    fun `buildUrl with valid path and custom size returns full URL`() {
        val result = ImageUrlBuilder.buildUrl("/abc123.jpg", ImageUrlBuilder.SIZE_ORIGINAL)

        assertEquals("https://image.tmdb.org/t/p/original/abc123.jpg", result)
    }

    @Test
    fun `buildUrl with null path returns null`() {
        val result = ImageUrlBuilder.buildUrl(null)

        assertNull(result)
    }

    @Test
    fun `buildPosterUrl with valid path returns W500 URL`() {
        val result = ImageUrlBuilder.buildPosterUrl("/poster.jpg")

        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", result)
    }

    @Test
    fun `buildPosterUrl with null path returns null`() {
        val result = ImageUrlBuilder.buildPosterUrl(null)

        assertNull(result)
    }

    @Test
    fun `buildBackdropUrl with valid path returns original size URL`() {
        val result = ImageUrlBuilder.buildBackdropUrl("/backdrop.jpg")

        assertEquals("https://image.tmdb.org/t/p/original/backdrop.jpg", result)
    }

    @Test
    fun `buildBackdropUrl with null path returns null`() {
        val result = ImageUrlBuilder.buildBackdropUrl(null)

        assertNull(result)
    }

    @Test
    fun `buildProfileUrl with valid path returns W185 URL`() {
        val result = ImageUrlBuilder.buildProfileUrl("/profile.jpg")

        assertEquals("https://image.tmdb.org/t/p/w185/profile.jpg", result)
    }

    @Test
    fun `buildProfileUrl with null path returns null`() {
        val result = ImageUrlBuilder.buildProfileUrl(null)

        assertNull(result)
    }

    @Test
    fun `buildThumbnailUrl with valid path returns W185 URL`() {
        val result = ImageUrlBuilder.buildThumbnailUrl("/thumb.jpg")

        assertEquals("https://image.tmdb.org/t/p/w185/thumb.jpg", result)
    }

    @Test
    fun `buildThumbnailUrl with null path returns null`() {
        val result = ImageUrlBuilder.buildThumbnailUrl(null)

        assertNull(result)
    }
}
