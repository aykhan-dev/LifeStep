package az.rabita.lifestep

import android.app.Application
import android.content.Context
import android.content.Intent
import az.rabita.lifestep.manager.LocaleManager
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.pojo.dataHolder.NotificationInfoHolder
import az.rabita.lifestep.ui.activity.main.MainActivity
import az.rabita.lifestep.utils.*
import com.onesignal.OneSignal
import timber.log.Timber

class LifeStepsApplication : Application() {

    private lateinit var sharedPreferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        sharedPreferenceManager = PreferenceManager.getInstance(applicationContext)

        val notificationOpenHandler = OneSignal.NotificationOpenedHandler { result ->
            val data = result?.notification?.payload?.additionalData
            data?.let {
                val userId = data.getString(NOTIFICATION_USER_ID_KEY)
                val type = data.getInt(NOTIFICATION_TYPE_KEY)
                val holder = NotificationInfoHolder(type, userId)
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(NOTIFICATION_INFO_KEY, holder)
                startActivity(intent)
            } // ?: toast("Notification Data is null")
        }

        OneSignal
            .startInit(applicationContext)
            .setNotificationOpenedHandler(notificationOpenHandler)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        //TODO delete these lines before the deployment
        Timber.e(OneSignal.getPermissionSubscriptionState().subscriptionStatus.userId)
        Timber.e(sharedPreferenceManager.getStringElement(TOKEN_KEY, ""))

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleManager.onAttach(base, DEFAULT_LANG_KEY))
    }

}