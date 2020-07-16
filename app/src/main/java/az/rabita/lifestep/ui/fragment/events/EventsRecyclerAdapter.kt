package az.rabita.lifestep.ui.fragment.events

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.pojo.apiPOJO.entity.Category

class EventsRecyclerAdapter(
    private val clickListener: (eventId: Int) -> Unit
) : RecyclerView.Adapter<EventsViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer(this, CategoryDiffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventsViewHolder {
        return EventsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: EventsViewHolder, position: Int) =
        with(asyncListDiffer.currentList) {
            holder.bind(get(position), clickListener)
        }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    fun submitList(list: List<Category>) {
        asyncListDiffer.submitList(list)
    }

}