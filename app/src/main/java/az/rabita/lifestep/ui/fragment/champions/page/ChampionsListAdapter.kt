package az.rabita.lifestep.ui.fragment.champions.page

import az.rabita.lifestep.R
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO
import az.rabita.lifestep.ui.fragment.ranking.RankerDiffCallback
import az.rabita.lifestep.utils.lib.DataBindingRecyclerAdapter

class ChampionsListAdapter(
    clickListener: (RankerContentPOJO, Int) -> Unit
) : DataBindingRecyclerAdapter<RankerContentPOJO>(
    RankerDiffCallback,
    clickListener = clickListener
) {

    override fun getItemViewType(position: Int): Int = R.layout.item_ranking

}