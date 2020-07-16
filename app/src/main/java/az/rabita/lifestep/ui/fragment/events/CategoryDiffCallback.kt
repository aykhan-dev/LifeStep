package az.rabita.lifestep.ui.fragment.events

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.entity.Category

object CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
    override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
        return oldItem == newItem
    }
}