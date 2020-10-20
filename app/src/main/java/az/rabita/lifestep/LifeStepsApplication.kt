package az.rabita.lifestep

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import az.rabita.lifestep.broadcast.LocalNotification
import az.rabita.lifestep.manager.PreferenceManager
import az.rabita.lifestep.pojo.dataHolder.NotificationInfoHolder
import az.rabita.lifestep.ui.activity.main.MainActivity
import az.rabita.lifestep.utils.LOCAL_NOTIFICATION_CODE
import az.rabita.lifestep.utils.NOTIFICATION_INFO_KEY
import az.rabita.lifestep.utils.NOTIFICATION_TYPE_KEY
import az.rabita.lifestep.utils.NOTIFICATION_USER_ID_KEY
import com.onesignal.OneSignal
import com.yariksoffice.lingver.Lingver
import timber.log.Timber
import java.util.*

class LifeStepsApplication : Application() {

    private lateinit var sharedPreferenceManager: PreferenceManager

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        sharedPreferenceManager = PreferenceManager.getInstance(applicationContext)

        Lingver.init(this, sharedPreferenceManager.language)

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
            }
        }

        OneSignal
            .startInit(applicationContext)
            .setNotificationOpenedHandler(notificationOpenHandler)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        configureDailyNotifications()

    }

    private fun configureDailyNotifications() {

        createNotificationChannel()

        val intent = Intent(applicationContext, LocalNotification::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            LOCAL_NOTIFICATION_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 22)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        manager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Timber.i("✅ Daily notifications have been set")

    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "LifeStep Reminder Channel"
            val description = "Channel for LifeStep reminder"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(LocalNotification.CHANNEL_NAME, name, importance)

            channel.description = description

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

    }

}