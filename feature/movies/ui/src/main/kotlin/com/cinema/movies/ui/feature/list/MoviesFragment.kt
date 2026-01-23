package com.cinema.movies.ui.feature.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.cinema.movies.ui.navigation.MovieNavigationDeeplink
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.ui.base.BaseFragment
import com.cinema.core.ui.extension.gone
import com.cinema.core.ui.extension.visible
import com.cinema.movies.ui.databinding.FragmentMoviesBinding
import com.cinema.movies.ui.feature.list.adapter.MoviesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MoviesFragment : BaseFragment<FragmentMoviesBinding>() {

    private val viewModel: MoviesViewModel by viewModels()

    private lateinit var moviesAdapter: MoviesAdapter

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMoviesBinding {
        return FragmentMoviesBinding.inflate(inflater, container, false)
    }

    override fun setupViews() {
        setupRecyclerView()
        setupTimeWindowToggle()
        setupErrorView()
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        moviesAdapter = MoviesAdapter { movie ->
            navigateToMovieDetail(movie.id)
        }
        binding.moviesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = moviesAdapter
        }
    }

    private fun navigateToMovieDetail(movieId: Int) {
        findNavController().navigate(Uri.parse(MovieNavigationDeeplink.detail(movieId)))
    }

    private fun setupTimeWindowToggle() {
        binding.timeWindowToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val timeWindow = when (checkedId) {
                    binding.buttonToday.id -> TimeWindow.DAY
                    binding.buttonWeek.id -> TimeWindow.WEEK
                    else -> TimeWindow.DAY
                }
                viewModel.onTimeWindowChanged(timeWindow)
            }
        }
    }

    private fun setupErrorView() {
        binding.errorView.setOnRetryClickListener {
            viewModel.retry()
        }
    }

    private fun updateUi(state: MoviesUiState) {
        if (state.isLoading) {
            binding.loadingView.show()
            binding.moviesRecyclerView.gone()
            binding.errorView.hide()
        } else {
            binding.loadingView.hide()
        }

        state.error?.let { error ->
            binding.errorView.show(error)
            binding.moviesRecyclerView.gone()
        } ?: run {
            if (!state.isLoading) {
                binding.errorView.hide()
                binding.moviesRecyclerView.visible()
            }
        }

        moviesAdapter.submitList(state.movies)

        val buttonId = when (state.selectedTimeWindow) {
            TimeWindow.WEEK -> binding.buttonWeek.id
            else -> binding.buttonToday.id
        }
        binding.timeWindowToggle.check(buttonId)
    }
}
