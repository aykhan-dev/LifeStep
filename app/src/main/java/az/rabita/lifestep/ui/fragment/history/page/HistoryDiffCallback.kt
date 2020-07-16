package az.rabita.lifestep.ui.fragment.history.page

import androidx.recyclerview.widget.DiffUtil
import az.rabita.lifestep.pojo.apiPOJO.content.HistoryItemContentPOJO

object HistoryDiffCallback : DiffUtil.ItemCallback<HistoryItemContentPOJO>() {

    override fun areItemsTheSame(
        oldItem: HistoryItemContentPOJO,
        newItem: HistoryItemContentPOJO
    ): Boolean {
        return oldItem.date == newItem.date
    }

    override fun areContentsTheSame(
        oldItem: HistoryItemContentPOJO,
        newItem: HistoryItemContentPOJO
    ): Boolean {
        return oldItem == newItem
    }

}