package az.rabita.lifestep.ui.fragment.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import az.rabita.lifestep.databinding.FragmentAdsBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.ui.dialog.message.MessageDialog
import az.rabita.lifestep.ui.dialog.message.MessageType
import az.rabita.lifestep.utils.ERROR_TAG
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.utils.toast
import az.rabita.lifestep.viewModel.fragment.ads.AdsViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.launch

class AdsFragment : Fragment() {

    private lateinit var binding: FragmentAdsBinding

    private val viewModel: AdsViewModel by viewModels()

    private val loadingDialog by lazy { LoadingDialog() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdsBinding.inflate(inflater)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchAdsPageContent()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindUI()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
        observeEvents()
    }

    private fun bindUI() = with(binding) {
        lifecycleOwner = this@AdsFragment
        viewModel = this@AdsFragment.viewModel

        buttonWatchAds.setOnClickListener {
            activity?.let { loadingDialog.show(it.supportFragmentManager, "Loading") }
            lifecycleScope.launch {
                loadAnAd()
            }
        }
    }

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
                viewModel.getBonusSteps()
            }
        })
    }

    private fun observeData(): Unit = with(viewModel) {

        errorMessage.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.let { activity ->
                    MessageDialog(MessageType.ERROR, it).show(
                        activity.supportFragmentManager,
                        ERROR_TAG
                    )
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpiredToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.logout()
                    endExpireTokenProcess()
                }
            }
        })

    }

}
