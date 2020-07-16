package az.rabita.lifestep.ui.fragment.contact

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.pojo.apiPOJO.entity.ContactDetail
import az.rabita.lifestep.pojo.apiPOJO.entity.Content

object ContactRecyclerAdapter : RecyclerView.Adapter<ContactViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer(this, ContactDiffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) =
        with(asyncListDiffer.currentList) {
            holder.bind(get(position))
        }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    fun submitList(list: List<Content>) {
        asyncListDiffer.submitList(list)
    }

}