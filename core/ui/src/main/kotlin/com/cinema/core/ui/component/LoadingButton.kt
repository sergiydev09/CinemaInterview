package com.cinema.core.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.cinema.core.ui.R
import com.cinema.core.ui.databinding.ViewLoadingButtonBinding

/**
 * Custom button that handles three states: disabled, enabled, and loading.
 * When loading, the button text is hidden and a progress indicator is shown.
 */
class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewLoadingButtonBinding
    private var buttonText: String = ""

    init {
        binding = ViewLoadingButtonBinding.inflate(LayoutInflater.from(context), this, true)

        context.theme.obtainStyledAttributes(attrs, R.styleable.LoadingButton, 0, 0).apply {
            try {
                buttonText = getString(R.styleable.LoadingButton_buttonText) ?: ""

                val textColor = getColor(
                    R.styleable.LoadingButton_buttonTextColor,
                    ContextCompat.getColor(context, R.color.on_secondary)
                )
                val backgroundColor = getColor(
                    R.styleable.LoadingButton_buttonBackgroundColor,
                    ContextCompat.getColor(context, R.color.secondary)
                )
                val indicatorColor = getColor(
                    R.styleable.LoadingButton_loadingIndicatorColor,
                    ContextCompat.getColor(context, R.color.on_secondary)
                )

                binding.button.text = buttonText
                binding.button.setTextColor(textColor)
                binding.button.setBackgroundColor(backgroundColor)
                binding.progressIndicator.setIndicatorColor(indicatorColor)
            } finally {
                recycle()
            }
        }
    }

    /**
     * Sets the button to disabled state.
     */
    fun setDisabled() {
        binding.button.isEnabled = false
        binding.button.text = buttonText
        binding.progressIndicator.visibility = GONE
    }

    /**
     * Sets the button to enabled state.
     */
    fun setEnabled() {
        binding.button.isEnabled = true
        binding.button.text = buttonText
        binding.progressIndicator.visibility = GONE
    }

    /**
     * Sets the button to loading state.
     * Hides the text and shows the progress indicator.
     */
    fun setLoading() {
        binding.button.isEnabled = false
        binding.button.text = ""
        binding.progressIndicator.visibility = VISIBLE
    }

    /**
     * Sets the click listener for the button.
     */
    fun setOnButtonClickListener(listener: OnClickListener) {
        binding.button.setOnClickListener(listener)
    }

    /**
     * Updates the button text.
     */
    fun setText(text: String) {
        buttonText = text
        if (binding.progressIndicator.visibility != VISIBLE) {
            binding.button.text = text
        }
    }
}
