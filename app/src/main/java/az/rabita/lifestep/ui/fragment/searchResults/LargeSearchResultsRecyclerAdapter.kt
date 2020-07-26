package az.rabita.lifestep.ui.fragment.searchResults

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.pojo.apiPOJO.content.SearchResultContentPOJO
import az.rabita.lifestep.ui.fragment.home.SearchResultDiffCallback

class LargeSearchResultsRecyclerAdapter(
    private val clickListener: (userId: String) -> Unit
) : RecyclerView.Adapter<LargeSearchResultsViewHolder>() {

    private val asyncListDiffer = AsyncListDiffer(this, SearchResultDiffCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LargeSearchResultsViewHolder {
        return LargeSearchResultsViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LargeSearchResultsViewHolder, position: Int) =
        with(asyncListDiffer.currentList) {
            holder.bind(get(position), clickListener)
        }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    fun submitList(list: List<SearchResultContentPOJO>) {
        asyncListDiffer.submitList(list)
    }

}