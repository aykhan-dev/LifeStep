package az.rabita.lifestep.ui.fragment.donation

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.pojo.apiPOJO.entity.Assocation

class DonationRecyclerAdapter(
    private val clickListener: (assocationId: String) -> Unit
) : RecyclerView.Adapter<DonationViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer(this, DonationDiffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        return DonationViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) =
        with(asyncListDiffer.currentList) {
            holder.bind(get(position), clickListener)
        }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    fun submitList(list: List<Assocation>) {
        asyncListDiffer.submitList(list)
    }

}