package az.rabita.lifestep.ui.fragment.contact

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemContactDetailsBinding
import az.rabita.lifestep.pojo.apiPOJO.entity.Content

class ContactViewHolder private constructor(
    private val binding: ItemContactDetailsBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        data: Content,
        clickListener: (content: Content) -> Unit
    ) = with(binding) {
        contactDetail = data
        textView.setOnClickListener { clickListener(data) }
    }

    companion object {

        fun from(parent: ViewGroup): ContactViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemContactDetailsBinding.inflate(inflater)
            return ContactViewHolder(binding)
        }

    }

}