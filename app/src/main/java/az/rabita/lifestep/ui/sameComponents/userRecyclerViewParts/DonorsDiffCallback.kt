package az.rabita.lifestep.ui.sameComponents.userRecyclerViewParts

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.content.DonorsContentPOJO

object DonorsDiffCallback : DiffUtil.ItemCallback<DonorsContentPOJO>() {

    override fun areItemsTheSame(oldItem: DonorsContentPOJO, newItem: DonorsContentPOJO): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DonorsContentPOJO,
        newItem: DonorsContentPOJO
    ): Boolean {
        return oldItem == newItem
    }

}