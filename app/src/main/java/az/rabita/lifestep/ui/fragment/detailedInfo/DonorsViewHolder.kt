package az.rabita.lifestep.ui.fragment.detailedInfo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemChampionsBinding
import az.rabita.lifestep.pojo.apiPOJO.content.DonorsContentPOJO

class DonorsViewHolder private constructor(private val binding: ItemChampionsBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(donorsContentPOJO: DonorsContentPOJO, clickListener: () -> Unit) = with(binding) {
        data = donorsContentPOJO
        root.setOnClickListener { clickListener() }
    }

    companion object {
        fun from(parent: ViewGroup): DonorsViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemChampionsBinding.inflate(layoutInflater, parent, false)
            return DonorsViewHolder(binding)
        }
    }

}