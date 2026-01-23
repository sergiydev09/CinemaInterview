package com.cinema.login.ui

/**
 * Interface for navigation events from the login feature.
 * Should be implemented by the hosting activity.
 */
interface LoginNavigator {
    /**
     * Called when login is successful and navigation to the private area should occur.
     */
    fun onLoginSuccess()
}
