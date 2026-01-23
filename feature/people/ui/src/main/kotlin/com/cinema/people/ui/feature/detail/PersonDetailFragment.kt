package com.cinema.people.ui.feature.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.cinema.core.ui.base.BaseFragment
import com.cinema.core.ui.extension.gone
import com.cinema.core.ui.extension.loadCircularImage
import com.cinema.core.ui.extension.visible
import com.cinema.people.ui.R
import com.cinema.people.ui.databinding.FragmentPersonDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class PersonDetailFragment : BaseFragment<FragmentPersonDetailBinding>() {

    private val viewModel: PersonDetailViewModel by viewModels()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPersonDetailBinding {
        return FragmentPersonDetailBinding.inflate(inflater, container, false)
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

    private fun updateUi(state: PersonDetailUiState) {
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

            val person = state.person ?: return

            errorView.hide()
            contentScrollView.visible()

            personName.text = person.name
            personDepartment.text = person.knownForDepartment
            personPopularity.text = String.format(Locale.US, "%.1f", person.popularity)

            personBiography.text = person.biography.ifEmpty {
                getString(R.string.no_biography)
            }

            person.birthday?.let { birthday ->
                birthdayLabel.visible()
                personBirthday.visible()
                personBirthday.text = birthday
            } ?: run {
                birthdayLabel.gone()
                personBirthday.gone()
            }

            person.placeOfBirth?.let { place ->
                placeOfBirthLabel.visible()
                personPlaceOfBirth.visible()
                personPlaceOfBirth.text = place
            } ?: run {
                placeOfBirthLabel.gone()
                personPlaceOfBirth.gone()
            }

            person.deathday?.let { deathday ->
                deathdayLabel.visible()
                personDeathday.visible()
                personDeathday.text = deathday
            } ?: run {
                deathdayLabel.gone()
                personDeathday.gone()
            }

            if (person.alsoKnownAs.isNotEmpty()) {
                alsoKnownAsLabel.visible()
                personAlsoKnownAs.visible()
                personAlsoKnownAs.text = person.alsoKnownAs.joinToString(", ")
            } else {
                alsoKnownAsLabel.gone()
                personAlsoKnownAs.gone()
            }

            personImage.loadCircularImage(
                url = person.profileUrl,
                placeholder = R.drawable.ic_placeholder_person
            )
        }
    }

    companion object {
        fun createArguments(personId: Int): Bundle {
            return bundleOf(PersonDetailViewModel.ARG_PERSON_ID to personId)
        }
    }
}
