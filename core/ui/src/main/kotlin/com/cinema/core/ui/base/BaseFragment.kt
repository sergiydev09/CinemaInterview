package com.cinema.core.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Base Fragment class that provides ViewBinding support.
 * All feature fragments should extend this class.
 *
 * @param VB The ViewBinding type for this fragment
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null

    /**
     * The ViewBinding instance for this fragment.
     * Only valid between onCreateView and onDestroyView.
     */
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException(
            "Binding is only valid between onCreateView and onDestroyView"
        )

    /**
     * Creates the ViewBinding instance.
     * Subclasses must implement this to provide their specific binding.
     */
    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = createBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeData()
    }

    /**
     * Called after the view is created to set up UI components.
     * Override this to initialize views, set click listeners, etc.
     */
    protected open fun setupViews() {}

    /**
     * Called after the view is created to set up data observation.
     * Override this to observe ViewModel data.
     */
    protected open fun observeData() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
