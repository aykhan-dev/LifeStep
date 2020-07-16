package az.rabita.lifestep.ui.fragment.contact

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.entity.Content

object ContactDiffCallback : DiffUtil.ItemCallback<Content>() {

    override fun areItemsTheSame(
        oldItem: Content,
        newItem: Content
    ): Boolean {
        return oldItem.contentKey == newItem.contentKey
    }

    override fun areContentsTheSame(
        oldItem: Content,
        newItem: Content
    ): Boolean {
        return oldItem == newItem
    }


}