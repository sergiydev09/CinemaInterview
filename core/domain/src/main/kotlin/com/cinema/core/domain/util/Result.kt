package com.cinema.core.domain.util

/**
 * A sealed class representing the result of an async operation.
 *
 * @param T The type of data being loaded
 */
sealed class Result<out T> {

    /**
     * Represents a successful data load.
     * @param data The loaded data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents an error during data loading.
     * @param message The error message
     * @param throwable Optional throwable for debugging
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : Result<Nothing>()

    /**
     * Represents a loading state.
     */
    data object Loading : Result<Nothing>()
}
