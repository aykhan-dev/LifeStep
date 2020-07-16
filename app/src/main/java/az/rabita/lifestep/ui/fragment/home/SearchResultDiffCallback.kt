package az.rabita.lifestep.ui.fragment.home

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO

object SearchResultDiffCallback : DiffUtil.ItemCallback<SearchResultContentPOJO>() {

    override fun areItemsTheSame(
        oldItem: SearchResultContentPOJO,
        newItem: SearchResultContentPOJO
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: SearchResultContentPOJO,
        newItem: SearchResultContentPOJO
    ): Boolean {
        return oldItem == newItem
    }

}