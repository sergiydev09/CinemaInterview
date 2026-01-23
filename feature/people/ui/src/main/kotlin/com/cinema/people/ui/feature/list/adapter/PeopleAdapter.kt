package com.cinema.people.ui.feature.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cinema.core.ui.extension.loadCircularImage
import com.cinema.people.domain.model.Person
import com.cinema.people.ui.R
import com.cinema.people.ui.databinding.ItemPersonBinding

/**
 * RecyclerView adapter for displaying people in a list.
 */
class PeopleAdapter(
    private val onPersonClick: (Person) -> Unit = {}
) : ListAdapter<Person, PeopleAdapter.PersonViewHolder>(PersonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemPersonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PersonViewHolder(binding, onPersonClick)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PersonViewHolder(
        private val binding: ItemPersonBinding,
        private val onPersonClick: (Person) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(person: Person) {
            with(binding) {
                personName.text = person.name
                personDepartment.text = person.knownForDepartment

                // Show known for titles
                val knownForText = person.knownFor
                    .take(3)
                    .joinToString(", ") { it.title }
                personKnownFor.text = knownForText.ifEmpty { "No known works" }

                personImage.loadCircularImage(
                    url = person.profileUrl,
                    placeholder = R.drawable.ic_placeholder_person
                )

                root.setOnClickListener { onPersonClick(person) }
            }
        }
    }

    private class PersonDiffCallback : DiffUtil.ItemCallback<Person>() {
        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem == newItem
        }
    }
}
