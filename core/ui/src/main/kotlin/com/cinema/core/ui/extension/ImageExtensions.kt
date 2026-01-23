package com.cinema.core.ui.extension

import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation

/**
 * Loads an image from a URL into the ImageView using Coil.
 * @param url The URL of the image
 * @param placeholder Optional placeholder drawable resource
 */
fun ImageView.loadImage(url: String?, placeholder: Int? = null) {
    load(url) {
        crossfade(true)
        placeholder?.let { placeholder(it) }
        error(placeholder ?: 0)
    }
}

/**
 * Loads a circular image from a URL into the ImageView using Coil.
 * @param url The URL of the image
 * @param placeholder Optional placeholder drawable resource
 */
fun ImageView.loadCircularImage(url: String?, placeholder: Int? = null) {
    load(url) {
        crossfade(true)
        transformations(CircleCropTransformation())
        placeholder?.let { placeholder(it) }
        error(placeholder ?: 0)
    }
}
