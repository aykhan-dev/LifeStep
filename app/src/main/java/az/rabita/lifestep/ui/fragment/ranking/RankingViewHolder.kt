package az.rabita.lifestep.ui.fragment.ranking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ItemRankingBinding
import az.rabita.lifestep.pojo.apiPOJO.content.RankerContentPOJO

class RankingViewHolder private constructor(
    private val binding: ItemRankingBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        rankerContentPOJO: RankerContentPOJO,
        clickListener: () -> Unit,
        position: Int,
        showMedals: Boolean
    ) = with(binding) {
        data = rankerContentPOJO

        if (showMedals) {
            when (position) {
                0 -> imageViewMedal.setImageResource(R.drawable.ic_medal_first)
                1 -> imageViewMedal.setImageResource(R.drawable.ic_medal_second)
                2 -> imageViewMedal.setImageResource(R.drawable.ic_medal_third)
            }
        }

        root.setOnClickListener { clickListener() }
    }

    companion object {

        fun from(parent: ViewGroup): RankingViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemRankingBinding.inflate(inflater, parent, false)
            return RankingViewHolder(binding)
        }

    }

}