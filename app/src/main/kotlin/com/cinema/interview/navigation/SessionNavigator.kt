package com.cinema.interview.navigation

import android.app.Activity
import android.content.Intent
import com.cinema.interview.presentation.PrivateActivity
import com.cinema.interview.presentation.PublicActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionNavigator @Inject constructor() {

    fun navigateToPrivateArea(activity: Activity) {
        val intent = Intent(activity, PrivateActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }

    fun navigateToLogin(activity: Activity) {
        val intent = Intent(activity, PublicActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        activity.startActivity(intent)
        activity.finish()
    }
}
