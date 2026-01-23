package com.cinema.core.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.cinema.core.ui.databinding.ViewLoadingBinding
import com.cinema.core.ui.extension.gone
import com.cinema.core.ui.extension.visible

/**
 * Custom loading view that displays a progress indicator with optional message.
 * Can be used across all features for consistent loading states.
 */
class LoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewLoadingBinding

    init {
        binding = ViewLoadingBinding.inflate(LayoutInflater.from(context), this, true)
    }

    /**
     * Shows the loading view with an optional message.
     * @param message Optional message to display below the progress indicator
     */
    fun show(message: String? = null) {
        visible()
        message?.let {
            binding.loadingMessage.text = it
            binding.loadingMessage.visible()
        } ?: binding.loadingMessage.gone()
    }

    /**
     * Hides the loading view.
     */
    fun hide() {
        gone()
    }
}
