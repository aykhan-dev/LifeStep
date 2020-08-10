package az.rabita.lifestep.utils

import android.os.CountDownTimer

class ExtendedCountDownTimer(
    private val timeTotal: Long,
    private val tickTime: Long,
    private val onFinishListener: () -> Unit,
    private val onTickListener: (Long) -> Unit
) {

    private lateinit var timer: CountDownTimer

    private var remainTime: Long = timeTotal

    fun start() {
        timer = object : CountDownTimer(remainTime, tickTime) {
            override fun onFinish() {
                onFinishListener()
            }

            override fun onTick(remainingTime: Long) {
                remainTime = remainingTime
                onTickListener(remainingTime)
            }

        }
        timer.start()
    }

    fun cancel() {
        if (::timer.isInitialized) timer.cancel()
    }

}