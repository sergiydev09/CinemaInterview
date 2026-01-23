package com.cinema.movies.ui.feature.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cinema.core.ui.base.BaseFragment
import com.cinema.core.ui.extension.gone
import com.cinema.core.ui.extension.loadImage
import com.cinema.core.ui.extension.visible
import com.cinema.movies.ui.R
import com.cinema.movies.ui.databinding.FragmentMovieDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class MovieDetailFragment : BaseFragment<FragmentMovieDetailBinding>() {

    private val viewModel: MovieDetailViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMovieDetailBinding {
        return FragmentMovieDetailBinding.inflate(inflater, container, false)
    }

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.errorView.setOnRetryClickListener {
            viewModel.retry()
        }
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

    private fun updateUi(state: MovieDetailUiState) {
        with(binding) {
            if (state.isLoading) {
                loadingView.show()
                contentScrollView.gone()
                errorView.hide()
                return
            }

            loadingView.hide()

            state.error?.let { error ->
                errorView.show(error)
                contentScrollView.gone()
                return
            }

            val movie = state.movie ?: return

            errorView.hide()
            contentScrollView.visible()

            movieTitle.text = movie.title
            movieRating.text = String.format(Locale.US, "%.1f", movie.voteAverage)
            movieVoteCount.text = getString(R.string.vote_count_format, movie.voteCount)
            movieReleaseDate.text = movie.releaseDate.take(4)

            movie.runtime?.let { runtime ->
                movieRuntime.visible()
                movieRuntime.text = getString(R.string.runtime_format, runtime)
            } ?: movieRuntime.gone()

            movie.tagline?.takeIf { it.isNotEmpty() }?.let { tagline ->
                movieTagline.visible()
                movieTagline.text = "\"$tagline\""
            } ?: movieTagline.gone()

            movieOverview.text = movie.overview.ifEmpty {
                getString(R.string.no_overview)
            }

            if (movie.genres.isNotEmpty()) {
                genresLabel.visible()
                movieGenres.visible()
                movieGenres.text = movie.genres.joinToString(", ") { it.name }
            } else {
                genresLabel.gone()
                movieGenres.gone()
            }

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
            if (movie.budget > 0) {
                budgetLabel.visible()
                movieBudget.visible()
                movieBudget.text = currencyFormat.format(movie.budget)
            } else {
                budgetLabel.gone()
                movieBudget.gone()
            }

            if (movie.revenue > 0) {
                revenueLabel.visible()
                movieRevenue.visible()
                movieRevenue.text = currencyFormat.format(movie.revenue)
            } else {
                revenueLabel.gone()
                movieRevenue.gone()
            }

            if (movie.productionCompanies.isNotEmpty()) {
                productionLabel.visible()
                movieProductionCompanies.visible()
                movieProductionCompanies.text = movie.productionCompanies.joinToString(", ")
            } else {
                productionLabel.gone()
                movieProductionCompanies.gone()
            }

            movieBackdrop.loadImage(
                url = movie.backdropUrl,
                placeholder = R.drawable.ic_placeholder_movie
            )
        }
    }

    companion object {
        fun createArguments(movieId: Int): Bundle {
            return bundleOf(MovieDetailViewModel.ARG_MOVIE_ID to movieId)
        }
    }
}
