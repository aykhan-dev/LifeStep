package az.rabita.lifestep.ui.fragment.donation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ItemComingToYouBinding
import az.rabita.lifestep.pojo.apiPOJO.entity.Assocation

class DonationViewHolder(private val binding: ItemComingToYouBinding) :
    RecyclerView.ViewHolder(binding.root) {

    @SuppressLint("UseCompatLoadingForDrawables")
    fun bind(
        donationItemPOJO: Assocation,
        clickListener: (assocationId: String) -> Unit
    ): Unit = with(binding) {
        data = donationItemPOJO
        val percent = donationItemPOJO.percent
        root.context?.let {
            progressBar.progressDrawable = it.getDrawable(
                when (percent) {
                    in 0..30 -> R.drawable.progress_bar_rounded_low
                    in 31..70 -> R.drawable.progress_bar_rounded_medium
                    else -> R.drawable.progress_bar_rounded_high
                }
            )
        }
        root.setOnClickListener { clickListener(donationItemPOJO.id) }
    }

    companion object {
        fun from(parent: ViewGroup): DonationViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemComingToYouBinding.inflate(layoutInflater, parent, false)
            return DonationViewHolder(binding)
        }
    }
}