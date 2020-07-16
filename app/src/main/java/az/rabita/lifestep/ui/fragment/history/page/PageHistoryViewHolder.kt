package az.rabita.lifestep.ui.fragment.history.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.databinding.ItemHistoryBinding
import az.rabita.lifestep.pojo.apiPOJO.content.HistoryItemContentPOJO

class PageHistoryViewHolder(private val binding: ItemHistoryBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(historyItemContentPOJO: HistoryItemContentPOJO) = with(binding) {
        data = historyItemContentPOJO
    }

    companion object {
        fun from(viewGroup: ViewGroup): PageHistoryViewHolder {
            val inflater = LayoutInflater.from(viewGroup.context)
            val binding = ItemHistoryBinding.inflate(inflater, viewGroup, false)
            return PageHistoryViewHolder(binding)
        }
    }

}