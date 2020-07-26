package az.rabita.lifestep.ui.activity.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import az.rabita.lifestep.R
import az.rabita.lifestep.databinding.ActivitySplashBinding
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.ui.activity.auth.AuthActivity
import az.rabita.lifestep.ui.activity.main.MainActivity
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import az.rabita.lifestep.utils.NOTIFICATION_CLICKED_KEY
import az.rabita.lifestep.utils.TOKEN_KEY
import com.bumptech.glide.Glide

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private val sharedPreferences by lazy { PreferenceManager.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        binding.apply {
            lifecycleOwner = this@SplashActivity
        }

        setUpLogoGIF()

        Handler().postDelayed({
            val intent =
                if (sharedPreferences.getStringElement(TOKEN_KEY, "").isEmpty())
                    Intent(applicationContext, AuthActivity::class.java)
                else
                    Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 1500)

    }

    private fun setUpLogoGIF() = with(binding) {
        Glide.with(applicationContext).load(R.raw.logo_gif).into(imageViewLogo)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(newBase, DEFAULT_LANG_KEY))
    }

}
