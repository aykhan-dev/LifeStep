package az.rabita.lifestep.ui.activity.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivitySplashNewBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.ui.activity.auth.AuthActivity
import az.rabita.lifestep.ui.activity.main.MainActivity
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashNewBinding
    private lateinit var timer: CountDownTimer
    private lateinit var mIntent: Intent

    private val sharedPreferences by lazy { PreferenceManager.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_new)
        bindUI()
        lifecycleScope.launch {
            mIntent = Intent(
                applicationContext,
                if (sharedPreferences.getStringElement(TOKEN_KEY, "").isEmpty())
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

            setUpLogoGIF()
        }
    }

    private fun bindUI(): Unit = with(binding) {
        lifecycleOwner = this@SplashActivity
    }

    private fun setUpLogoGIF(): Unit = with(binding) {
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

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(newBase, DEFAULT_LANG_KEY))
    }

}