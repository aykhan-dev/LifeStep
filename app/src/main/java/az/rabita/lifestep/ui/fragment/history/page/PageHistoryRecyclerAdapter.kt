package az.rabita.lifestep.ui.fragment.history.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import az.rabita.lifestep.databinding.ItemHistoryBinding
import az.rabita.lifestep.pojo.apiPOJO.content.HistoryItemContentPOJO

class PageHistoryRecyclerAdapter : PagingDataAdapter<HistoryItemContentPOJO, PageHistoryViewHolder>(HistoryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHistoryViewHolder {
        return PageHistoryViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: PageHistoryViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

}