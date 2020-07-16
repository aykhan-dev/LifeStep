package az.rabita.lifestep.utils

import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import az.rabita.lifestep.R
import az.rabita.lifestep.ui.custom.BarDiagram
import az.rabita.lifestep.ui.custom.DailyStatCard
import az.rabita.lifestep.ui.custom.WalletCard
import com.bumptech.glide.Glide
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView

@BindingAdapter("icon_donation")
fun ImageView.selectDonation(index: Int) {
    if (index == 0) this.setImageResource(R.drawable.ic_donate_round)
    else this.setImageResource(R.drawable.ic_donate)
}

@BindingAdapter("icon_wallet")
fun ImageView.selectWallet(index: Int) {
    if (index == 1) this.setImageResource(R.drawable.ic_wallet_round)
    else this.setImageResource(R.drawable.ic_wallet)
}

@BindingAdapter("icon_ads")
fun ImageView.selectAds(index: Int) {
    if (index == 3) this.setImageResource(R.drawable.ic_present_round)
    else this.setImageResource(R.drawable.ic_present)
}

@BindingAdapter("icon_settings")
fun ImageView.selectSettings(index: Int) {
    if (index == 4) this.setImageResource(R.drawable.ic_settings_round)
    else this.setImageResource(R.drawable.ic_settings)
}

@BindingAdapter("data")
fun BarDiagram.bindDiagram(data: BarDiagram.DiagramDataModel?) {
    data?.let { submitData(it) }
}

@BindingAdapter("uiState")
fun ProgressBar.bindUiState(state: UiState) {
    this.isVisible = when (state) {
        UiState.Loading -> true
        else -> false
    }
}

@BindingAdapter("pagerAdapter")
fun ViewPager2.setupAdapter(adapter: FragmentStateAdapter) {
    this.adapter = adapter
    this.isUserInputEnabled = false
}

@BindingAdapter("imageUrl")
fun ImageView.loadImage(url: String?) {
    url?.let {
        if (url.endsWith("svg")) {
            GlideToVectorYou
                .init()
                .with(context)
                .load(Uri.parse(url), this)
        } else {
            Glide.with(context).load(url).into(this)
        }
    }
}

@BindingAdapter("isBold")
fun MaterialTextView.isBold(boolean: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        setTextAppearance(
            if (boolean) R.style.styleProfileDiagramTabSelectedText else R.style.styleProfileDiagramTabUnselectedText
        )
    } else {
        setTextAppearance(
            context,
            if (boolean) R.style.styleProfileDiagramTabSelectedText else R.style.styleProfileDiagramTabUnselectedText
        )
    }
}

@BindingAdapter("countData")
fun WalletCard.setCount(data: String?) {
    count.text = data
}

@BindingAdapter("isVisible")
fun View.isVisible(flag: Boolean) {
    this.isVisible = flag
}

@BindingAdapter("hint")
fun TextInputEditText.setHint(data: Any) {
    hint = data.toString()
}

@BindingAdapter("textContent")
fun MaterialTextView.setText(data: Any) {
    text = data.toString()
}

@BindingAdapter("count")
fun DailyStatCard.setCount(data: Any) {
    count = data.toString()
}

@BindingAdapter("dateText")
fun MaterialTextView.setFormattedDateAsText(date: String) {
    text = date.substring(0, 10)
}