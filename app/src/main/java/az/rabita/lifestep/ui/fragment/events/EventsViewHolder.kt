package az.rabita.lifestep.ui.fragment.events

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemEventsBinding
import az.rabita.lifestep.pojo.apiPOJO.entity.Category

class EventsViewHolder private constructor(
    private val binding: ItemEventsBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(category: Category, clickListener: (eventId: Int) -> Unit) = with(binding) {
        data = category
        root.setOnClickListener {
            if (category.isReady) {
                textViewPreparing.visibility = GONE
                clickListener(category.id)
            } else textViewPreparing.visibility = VISIBLE
        }
    }

    companion object {

        fun from(parent: ViewGroup): EventsViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemEventsBinding.inflate(inflater, parent, false)
            return EventsViewHolder(binding)
        }

    }

}