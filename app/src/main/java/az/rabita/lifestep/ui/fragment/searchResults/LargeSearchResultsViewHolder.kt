package az.rabita.lifestep.ui.fragment.searchResults

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemSearchResultsLargeBinding
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO

class LargeSearchResultsViewHolder private constructor(
    private val binding: ItemSearchResultsLargeBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        searchResultContentPOJO: SearchResultContentPOJO,
        clickListener: (userId: String) -> Unit
    ): Unit = with(binding) {
        data = searchResultContentPOJO
        textViewFullName.text = "${searchResultContentPOJO.surname} ${searchResultContentPOJO.name}"
        root.setOnClickListener { clickListener(searchResultContentPOJO.id) }
    }

    companion object {

        fun from(viewGroup: ViewGroup): LargeSearchResultsViewHolder {
            val inflater = LayoutInflater.from(viewGroup.context)
            val binding = ItemSearchResultsLargeBinding.inflate(inflater, viewGroup, false)
            return LargeSearchResultsViewHolder(binding)
        }

    }

}