package az.rabita.lifestep

import android.app.Application
import android.content.Context
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.utils.DEFAULT_LANG_KEY
import com.onesignal.OneSignal
import timber.log.Timber

class LifeStepsApplication : Application() {

    private lateinit var sharedPreferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        sharedPreferenceManager = PreferenceManager.getInstance(applicationContext)

        OneSignal
            .startInit(applicationContext)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(base, DEFAULT_LANG_KEY))
    }

}