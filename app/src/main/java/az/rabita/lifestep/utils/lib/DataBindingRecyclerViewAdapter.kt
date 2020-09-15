package az.rabita.lifestep.utils.lib

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.BR

open class DataBindingRecyclerAdapter<T>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val clickListener: ((item: T, position: Int) -> Unit)? = null
) : ListAdapter<T, DataBindingViewHolder<T>>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding =
            DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
        return DataBindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder<T>, position: Int) {
        holder.bind(getItem(position), position, clickListener)
    }

}

class DataBindingViewHolder<T>(
    private val binding: ViewDataBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        item: T,
        position: Int,
        clickListener: ((item: T, position: Int) -> Unit)?
    ): Unit = with(binding) {
        setVariable(BR.data, item)
        clickListener?.let { root.setOnClickListener { clickListener(item, position) } }
        executePendingBindings()
    }

}