package az.rabita.lifestep.ui.fragment.donation

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.entity.Assocation

object DonationDiffCallback : DiffUtil.ItemCallback<Assocation>() {

    override fun areItemsTheSame(oldItem: Assocation, newItem: Assocation): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Assocation, newItem: Assocation): Boolean {
        return oldItem.id == newItem.id
    }

}