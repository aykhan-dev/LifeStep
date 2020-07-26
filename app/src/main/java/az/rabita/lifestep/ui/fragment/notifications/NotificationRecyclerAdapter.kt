package az.rabita.lifestep.ui.fragment.notifications

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.pojo.apiPOJO.entity.Notifications

class NotificationRecyclerAdapter(
    private val clickListener: (notification: Notifications) -> Unit
) : RecyclerView.Adapter<NotificationViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer(this, NotificationDiffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) =
        with(asyncListDiffer.currentList) {
            holder.bind(get(position), clickListener)
        }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    fun submitList(list: List<Notifications>) {
        asyncListDiffer.submitList(list)
    }

}