package az.rabita.lifestep.ui.dialog.ads

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentAdsDialogBinding
import az.rabita.lifestep.ui.dialog.loading.LoadingDialog
import az.rabita.lifestep.utils.LOADING_TAG
import az.rabita.lifestep.utils.UiState
import az.rabita.lifestep.utils.logout
import az.rabita.lifestep.utils.openUrl
import az.rabita.lifestep.viewModel.activity.ads.WatchingAdsViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class AdsDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentAdsDialogBinding

    private val viewModel: WatchingAdsViewModel by viewModels()

    private var simplePlayer: SimpleExoPlayer? = null

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    private var startTime = 0L

    private val args by navArgs<AdsDialogFragmentArgs>()
    private val loadingDialog by lazy { LoadingDialog() }

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdsDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bindUI()

        observeData()
        observeStates()
        observeEvents()
    }

    override fun getTheme(): Int = R.style.DialogTheme

    private fun bindUI() = with(binding) {
        imageViewClose.setOnClickListener {
            sendVideoResult()
            dismiss()
        }

        buttonDownload.setOnClickListener {
            openUrl(args.adsTransactionDetails.openingUrl ?: "")
        }

        data = args.adsTransactionDetails
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || simplePlayer == null) initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) releasePlayer()
    }

    private fun observeData(): Unit = with(viewModel) {}

    private fun observeStates(): Unit = with(viewModel) {

        uiState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    is UiState.Loading -> {
                        activity?.let { activity ->
                            loadingDialog.show(activity.supportFragmentManager, LOADING_TAG)
                        }
                    }
                    is UiState.LoadingFinished -> loadingDialog.dismiss()
                }
            }
        })

    }

    private fun observeEvents(): Unit = with(viewModel) {

        eventExpireToken.observe(viewLifecycleOwner, Observer {
            it?.let {
                activity?.logout()
                endExpireTokenProcess()
            }
        })

        eventCloseAdsPage.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    if (args.isForBonusSteps) navController.navigate(AdsDialogFragmentDirections.actionAdsDialogFragment2ToBonusStepDialog())
                    else dismiss()
                }
            }
        })

    }

    private fun initializePlayer(): Unit = with(binding) {
        simplePlayer = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = simplePlayer

        val uri = Uri.parse(args.adsTransactionDetails.videoUrl)
        val mediaSource = buildMediaSource(uri)

        simplePlayer!!.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when (playbackState) {
                    ExoPlayer.STATE_READY -> if (startTime == 0L) {
                        startTime = System.currentTimeMillis()
                    }
                    ExoPlayer.STATE_ENDED -> sendVideoResult()
                }
            }

        })

        simplePlayer!!.playWhenReady = (playWhenReady)
        simplePlayer!!.seekTo(currentWindow, playbackPosition)
        simplePlayer!!.prepare(mediaSource, false, false)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(requireContext(), "exoplayer-codelab")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }

    private fun releasePlayer() {
        simplePlayer?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            it.release()
            simplePlayer = null
        }
    }

    private fun sendVideoResult() {
        if (startTime != 0L) {
            val spentMillis = System.currentTimeMillis() - startTime
            var spentSeconds = (spentMillis / 1000).toInt()

            if (spentSeconds > args.adsTransactionDetails.watchTime)
                spentSeconds = args.adsTransactionDetails.watchTime

            this@AdsDialogFragment.viewModel.sendAdsTransactionResult(
                args.adsTransactionDetails.transactionId,
                spentSeconds,
                args.isForBonusSteps
            )
        }
    }

}