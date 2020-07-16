package az.rabita.lifestep.ui.fragment.detailedInfo

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.pojo.apiPOJO.content.DonorsContentPOJO
import az.rabita.lifestep.ui.sameComponents.userRecyclerViewParts.DonorsDiffCallback

class DonorsRecyclerAdapter(
    private val clickListener: () -> Unit
) : RecyclerView.Adapter<DonorsViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer(this, DonorsDiffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorsViewHolder {
        return DonorsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DonorsViewHolder, position: Int) =
        with(asyncListDiffer.currentList) {
            holder.bind(get(position), clickListener)
        }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    fun submitList(list: List<DonorsContentPOJO>) {
        asyncListDiffer.submitList(list)
    }

}