package com.cinema.core.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.cinema.core.ui.databinding.ViewErrorBinding
import com.cinema.core.ui.extension.gone
import com.cinema.core.ui.extension.visible

/**
 * Custom error view that displays an error message with a retry button.
 * Can be used across all features for consistent error states.
 */
class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewErrorBinding
    private var onRetryClickListener: (() -> Unit)? = null

    init {
        binding = ViewErrorBinding.inflate(LayoutInflater.from(context), this, true)
        binding.retryButton.setOnClickListener {
            onRetryClickListener?.invoke()
        }
    }

    /**
     * Shows the error view with a message.
     * @param message The error message to display
     */
    fun show(message: String) {
        visible()
        binding.errorMessage.text = message
    }

    /**
     * Hides the error view.
     */
    fun hide() {
        gone()
    }

    /**
     * Sets the retry button click listener.
     * @param listener The callback to invoke when retry is clicked
     */
    fun setOnRetryClickListener(listener: () -> Unit) {
        onRetryClickListener = listener
    }
}
