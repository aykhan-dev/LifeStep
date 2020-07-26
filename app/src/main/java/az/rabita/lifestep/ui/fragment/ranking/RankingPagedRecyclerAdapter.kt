package az.rabita.lifestep.ui.fragment.ranking

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO

class RankingPagedRecyclerAdapter(
    private val clickListener: (ranker: RankerContentPOJO) -> Unit
) : PagingDataAdapter<RankerContentPOJO, RankingViewHolder>(RankerDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        return RankingViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        getItem(position)?.let { pojo ->
            holder.bind(pojo, clickListener, position, false)
        }
    }

}