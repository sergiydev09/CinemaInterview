package com.cinema.core.ui.extension

import android.view.View

/**
 * Sets the visibility of a View to VISIBLE.
 */
fun View.visible() {
    visibility = View.VISIBLE
}

/**
 * Sets the visibility of a View to GONE.
 */
fun View.gone() {
    visibility = View.GONE
}

/**
 * Sets the visibility of a View to INVISIBLE.
 */
fun View.invisible() {
    visibility = View.INVISIBLE
}

/**
 * Sets the visibility of a View based on a condition.
 * @param condition If true, the view is visible; otherwise, it's gone
 */
fun View.visibleIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}
