package az.rabita.lifestep.ui.fragment.ranking

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO

object RankerDiffCallback : DiffUtil.ItemCallback<RankerContentPOJO>() {
    override fun areItemsTheSame(oldItem: RankerContentPOJO, newItem: RankerContentPOJO): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: RankerContentPOJO,
        newItem: RankerContentPOJO
    ): Boolean {
        return oldItem == newItem
    }
}