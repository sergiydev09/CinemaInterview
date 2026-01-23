package com.cinema.people.ui.feature.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.ui.base.BaseFragment
import com.cinema.core.ui.extension.gone
import com.cinema.core.ui.extension.visible
import com.cinema.people.ui.databinding.FragmentPeopleBinding
import com.cinema.people.ui.feature.list.adapter.PeopleAdapter
import com.cinema.people.ui.navigation.PeopleNavigationDeeplink
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PeopleFragment : BaseFragment<FragmentPeopleBinding>() {

    private val viewModel: PeopleViewModel by viewModels()

    private lateinit var peopleAdapter: PeopleAdapter

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPeopleBinding {
        return FragmentPeopleBinding.inflate(inflater, container, false)
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
        peopleAdapter = PeopleAdapter { person ->
            navigateToPersonDetail(person.id)
        }
        binding.peopleRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = peopleAdapter
        }
    }

    private fun navigateToPersonDetail(personId: Int) {
        findNavController().navigate(PeopleNavigationDeeplink.detail(personId).toUri())
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

    private fun updateUi(state: PeopleUiState) {
        // Loading state
        if (state.isLoading) {
            binding.loadingView.show()
            binding.peopleRecyclerView.gone()
            binding.errorView.hide()
        } else {
            binding.loadingView.hide()
        }

        // Error state
        state.error?.let { error ->
            binding.errorView.show(error)
            binding.peopleRecyclerView.gone()
        } ?: run {
            if (!state.isLoading) {
                binding.errorView.hide()
                binding.peopleRecyclerView.visible()
            }
        }

        // Data state
        peopleAdapter.submitList(state.people)

        // Update toggle selection
        val buttonId = when (state.selectedTimeWindow) {
            TimeWindow.WEEK -> binding.buttonWeek.id
            else -> binding.buttonToday.id
        }
        binding.timeWindowToggle.check(buttonId)
    }
}
