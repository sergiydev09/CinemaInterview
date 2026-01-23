package com.cinema.movies.ui.feature.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cinema.core.ui.extension.loadImage
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.ui.R
import com.cinema.movies.ui.databinding.ItemMovieBinding
import java.util.Locale

class MoviesAdapter(
    private val onMovieClick: (Movie) -> Unit = {}
) : ListAdapter<Movie, MoviesAdapter.MovieViewHolder>(MovieDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding, onMovieClick)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MovieViewHolder(
        private val binding: ItemMovieBinding,
        private val onMovieClick: (Movie) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            with(binding) {
                movieTitle.text = movie.title
                movieRating.text = String.format(Locale.US, "%.1f", movie.voteAverage)
                movieYear.text = movie.releaseDate.take(4)

                moviePoster.loadImage(
                    url = movie.posterUrl,
                    placeholder = R.drawable.ic_placeholder_movie
                )

                root.setOnClickListener { onMovieClick(movie) }
            }
        }
    }

    private class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}
