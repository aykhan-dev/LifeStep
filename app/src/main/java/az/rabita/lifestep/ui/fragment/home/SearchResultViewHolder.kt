package az.rabita.lifestep.ui.fragment.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemSearchResultBinding
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO
import az.rabita.lifestep.utils.loadImage

class SearchResultViewHolder private constructor(private val binding: ItemSearchResultBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(data: SearchResultContentPOJO, clickListener: (userId: String) -> Unit) =
        with(binding) {
            textViewName.text = "${data.surname} ${data.name}"
            imageViewProfile.loadImage(data.imageUrl)
            root.setOnClickListener { clickListener(data.id) }
        }

    companion object {

        fun from(viewGroup: ViewGroup): SearchResultViewHolder {
            val inflater = LayoutInflater.from(viewGroup.context)
            val binding = ItemSearchResultBinding.inflate(inflater, viewGroup, false)
            return SearchResultViewHolder(binding)
        }

    }

}