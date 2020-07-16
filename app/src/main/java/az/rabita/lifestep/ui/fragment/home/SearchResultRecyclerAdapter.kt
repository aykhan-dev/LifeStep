package az.rabita.lifestep.ui.fragment.home

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO

class SearchResultRecyclerAdapter(
    private val clickListener: (userId: String) -> Unit
) : RecyclerView.Adapter<SearchResultViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer(this, SearchResultDiffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        return SearchResultViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) =
        with(asyncListDiffer.currentList) {
            holder.bind(get(position), clickListener)
        }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    fun submitList(list: List<SearchResultContentPOJO>) {
        var data = list
        if (list.size > 5) data = list.subList(0, 5)
        asyncListDiffer.submitList(data)
    }

}