package az.rabita.lifestep.utils

/***
private fun loadAnAd() {
val rewardedAd = RewardedAd(context, "ca-app-pub-3940256099942544/5224354917")
val callback = object : RewardedAdLoadCallback() {
override fun onRewardedAdLoaded() {
loadingDialog.dismiss()
showLoadedAd(rewardedAd)
}

override fun onRewardedAdFailedToLoad(p0: Int) {
loadingDialog.dismiss()
context?.toast("Error while ad loading")
}
}
rewardedAd.loadAd(AdRequest.Builder().build(), callback)
}

private fun showLoadedAd(ad: RewardedAd) {
ad.show(requireActivity(), object : RewardedAdCallback() {
override fun onUserEarnedReward(p0: RewardItem) {
viewModel.convertSteps()
}
})
}
 ***/