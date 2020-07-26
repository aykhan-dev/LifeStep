package az.rabita.lifestep.ui.fragment.ranking

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO

class RankingRecyclerAdapter(
    private val showMedals: Boolean = false,
    private val clickListener: (ranker: RankerContentPOJO) -> Unit
) : ListAdapter<RankerContentPOJO, RankingViewHolder>(RankerDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        return RankingViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, position, showMedals)
    }

}