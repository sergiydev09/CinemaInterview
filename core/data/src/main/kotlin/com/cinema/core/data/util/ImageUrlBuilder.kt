package com.cinema.core.data.util

/**
 * Utility for building TMDB image URLs.
 * TMDB images require a base URL + size + path.
 */
object ImageUrlBuilder {
    private const val BASE_URL = "https://image.tmdb.org/t/p/"

    const val SIZE_ORIGINAL = "${BASE_URL}original"
    const val SIZE_W500 = "${BASE_URL}w500"
    const val SIZE_W185 = "${BASE_URL}w185"

    /**
     * Builds a full image URL from a path.
     * @param path The image path from the API (e.g., "/abc123.jpg")
     * @param size The size prefix to use (default: W500)
     * @return Full URL or null if path is null
     */
    fun buildUrl(path: String?, size: String = SIZE_W500): String? {
        return path?.let { "$size$it" }
    }

    /**
     * Builds a poster URL (W500 size, good for movie/TV posters).
     */
    fun buildPosterUrl(path: String?): String? = buildUrl(path, SIZE_W500)

    /**
     * Builds a backdrop URL (original size, good for backgrounds).
     */
    fun buildBackdropUrl(path: String?): String? = buildUrl(path, SIZE_ORIGINAL)

    /**
     * Builds a profile URL (W185 size, good for person photos).
     */
    fun buildProfileUrl(path: String?): String? = buildUrl(path, SIZE_W185)

    /**
     * Builds a thumbnail URL (W185 size, good for small images).
     */
    fun buildThumbnailUrl(path: String?): String? = buildUrl(path, SIZE_W185)
}
