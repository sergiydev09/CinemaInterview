package com.cinema.home.ui.feature

import android.view.LayoutInflater
import android.view.ViewGroup
import com.cinema.core.ui.base.BaseFragment
import com.cinema.home.ui.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment displaying the home screen with welcome message.
 * Acts as the main landing page after login.
 */
@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun setupViews() {
        // Home screen setup - static content for now
    }
}
