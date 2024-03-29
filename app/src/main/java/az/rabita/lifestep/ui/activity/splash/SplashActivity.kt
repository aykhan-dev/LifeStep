package az.rabita.lifestep.ui.activity.splash

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivitySplashNewBinding
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.ui.activity.BaseActivity
import az.rabita.lifestep.ui.activity.auth.AuthActivity
import az.rabita.lifestep.ui.activity.main.MainActivity


class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashNewBinding
    private lateinit var timer: CountDownTimer
    private lateinit var mIntent: Intent

    private val sharedPreferences by lazy { PreferenceManager.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mIntent = Intent(
            applicationContext,
            if (sharedPreferences.token.isEmpty())
                AuthActivity::class.java
            else
                MainActivity::class.java
        )

        timer = object : CountDownTimer(1500L, 1500L) {
            override fun onFinish() {
                startActivity(mIntent)
                finish()
            }

            override fun onTick(p0: Long) {}
        }
        timer.start()

        setUpLogo()
    }

    private fun setUpLogo(): Unit = with(binding) {
        imageViewLogo.startAnimation(
            AnimationUtils.loadAnimation(
                applicationContext,
                R.anim.beat_anim
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::timer.isInitialized) {
            timer.cancel()
            binding.imageViewLogo.clearAnimation()
        }
    }

}
