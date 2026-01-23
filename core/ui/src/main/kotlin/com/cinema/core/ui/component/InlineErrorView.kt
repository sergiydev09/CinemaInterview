package com.cinema.core.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.cinema.core.ui.databinding.ViewInlineErrorBinding
import com.cinema.core.ui.extension.gone
import com.cinema.core.ui.extension.visible

/**
 * Compact error view for displaying inline errors in forms.
 * Shows an icon with error message in a card format.
 */
class InlineErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewInlineErrorBinding

    init {
        binding = ViewInlineErrorBinding.inflate(LayoutInflater.from(context), this, true)
    }

    /**
     * Shows the error view with a message.
     * @param message The error message to display
     * @param title Optional title to display above the message
     */
    fun show(message: String, title: String? = null) {
        visible()
        binding.errorMessage.text = message
        title?.let {
            binding.errorTitle.text = it
            binding.errorTitle.visible()
        } ?: binding.errorTitle.gone()
    }

    /**
     * Hides the error view.
     */
    fun hide() {
        gone()
    }
}
