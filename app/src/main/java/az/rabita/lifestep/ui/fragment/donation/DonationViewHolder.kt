package az.rabita.lifestep.ui.fragment.donation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemComingToYouBinding
import az.rabita.lifestep.pojo.apiPOJO.entity.Assocation

class DonationViewHolder(private val binding: ItemComingToYouBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(
        donationItemPOJO: Assocation,
        clickListener: (assocationId: String) -> Unit
    ) = with(binding) {
        data = donationItemPOJO
        root.setOnClickListener { clickListener(donationItemPOJO.id) }
    }

    companion object {
        fun from(parent: ViewGroup): DonationViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemComingToYouBinding.inflate(layoutInflater, parent, false)
            return DonationViewHolder(binding)
        }
    }
}