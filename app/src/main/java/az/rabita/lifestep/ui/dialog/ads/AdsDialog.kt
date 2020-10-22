package az.rabita.lifestep.ui.dialog.ads

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.FragmentAdsDialogBinding
import az.rabita.lifestep.ui.dialog.SingleInstanceDialog
import az.rabita.lifestep.utils.ExtendedCountDownTimer
import az.rabita.lifestep.utils.openUrl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class AdsDialog : SingleInstanceDialog() {

    companion object {
        const val RESULT_KEY = "ads watching result"

        const val ID_KEY = "transaction id"
        const val WATCHED_TIME_KEY = "watched seconds"
        const val IS_FOR_BONUS_STEPS_KEY = "is for bonus steps"
        const val TOTAL_WATCH_TIME_KEY = "watch time of ads"
    }

    private lateinit var binding: FragmentAdsDialogBinding

    private val args by navArgs<AdsDialogArgs>()

    private val timer = Timer()

    private var simplePlayer: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private var startTime = 0L

    private val navController by lazy { findNavController() }

    private val isMuted = MutableLiveData(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FragmentAdsDialogBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurations()
        bindUI()

        timer.setup(timeMillis = args.adsTransactionDetails.watchTime * 1000L)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeData()
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

    private fun configurations() {
        isCancelable = false
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@AdsDialog
        data = args.adsTransactionDetails

        buttonDownload.setOnClickListener { openUrl(args.adsTransactionDetails.openingUrl) }
        imageViewClose.setOnClickListener { submitResult() }
        imageViewMute.setOnClickListener {
            isMuted.value = !isMuted.value!!
            imageViewMute.setImageResource(
                if (isMuted.value == true) R.drawable.ic_baseline_volume_off_24
                else R.drawable.ic_baseline_volume_up_24
            )
        }
    }

    private fun observeData() {

        timer.remainTime.observe(viewLifecycleOwner, Observer {
            it?.let {
                val seconds: Long = TimeUnit.SECONDS.toSeconds(it)
                val minutes: Long = TimeUnit.SECONDS.toMinutes(it)
                binding.textViewTime.text = String.format("%02d:%02d", minutes, seconds)
            }
        })

        isMuted.observe(viewLifecycleOwner, Observer {
            it?.let { simplePlayer?.volume = if (it) 0f else 1f }
        })

    }

    override fun getTheme(): Int = R.style.DialogTheme

    private fun initializePlayer(): Unit = with(binding) {

        simplePlayer = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = simplePlayer

        val uri = Uri.parse(args.adsTransactionDetails.videoUrl)
        val mediaSource = buildMediaSource(uri)

        simplePlayer!!.addListener(object : Player.EventListener {

            @SuppressLint("SwitchIntDef")
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                when (playbackState) {
                    ExoPlayer.STATE_READY -> if (startTime == 0L) {
                        startTime = System.currentTimeMillis()
                    }
                    ExoPlayer.STATE_ENDED -> submitResult()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) timer.start() else timer.pause()
            }

        })

        simplePlayer!!.volume = if (isMuted.value == true) 0f else 1f
        simplePlayer!!.playWhenReady = (playWhenReady)
        simplePlayer!!.seekTo(currentWindow, playbackPosition)
        simplePlayer!!.prepare(mediaSource, false, false)
    }

    private fun releasePlayer() {
        simplePlayer?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            it.release()
            simplePlayer = null
            timer.pause()
        }
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "exoplayer-codelab")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun submitResult() {

        if (startTime != 0L) {
            val watchedMillis = System.currentTimeMillis() - startTime
            var watchedSeconds = (watchedMillis / 1000f).roundToInt()

            if (watchedSeconds > args.adsTransactionDetails.watchTime)
                watchedSeconds = args.adsTransactionDetails.watchTime

            navController.previousBackStackEntry!!.savedStateHandle.set(
                RESULT_KEY,
                mapOf(
                    ID_KEY to args.adsTransactionDetails.transactionId,
                    WATCHED_TIME_KEY to watchedSeconds,
                    IS_FOR_BONUS_STEPS_KEY to args.isForBonusSteps,
                    TOTAL_WATCH_TIME_KEY to args.adsTransactionDetails.watchTime
                )
            )
        }

        navController.popBackStack()

    }

    class Timer {

        private lateinit var timer: ExtendedCountDownTimer

        private val _remainTime = MutableLiveData(0L)
        val remainTime: LiveData<Long> = _remainTime

        fun setup(timeMillis: Long = 15000L, tickMillis: Long = 1000L) {
            timer = ExtendedCountDownTimer(
                timeMillis,
                tickMillis,
                { _remainTime.value = null; },
                { _remainTime.value = it / 1000 }
            )
        }

        fun start(): Unit = timer.start()

        fun pause(): Unit = timer.cancel()

    }

}