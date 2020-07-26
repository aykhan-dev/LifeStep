package az.rabita.lifestep.manager

import android.content.Context
import android.content.Intent
import az.rabita.lifestep.pojo.dataHolder.NotificationInfoHolder
import az.rabita.lifestep.ui.activity.main.MainActivity
import az.rabita.lifestep.utils.NOTIFICATION_INFO_KEY
import az.rabita.lifestep.utils.NOTIFICATION_TYPE_KEY
import az.rabita.lifestep.utils.NOTIFICATION_USER_ID_KEY
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal

class NotificationManager(private val context: Context) : OneSignal.NotificationOpenedHandler {

    override fun notificationOpened(result: OSNotificationOpenResult?) {
        val data = result?.notification?.payload?.additionalData
        data?.let {
            val userId = data.getString(NOTIFICATION_USER_ID_KEY)
            val type = data.getInt(NOTIFICATION_TYPE_KEY)

            val holder = NotificationInfoHolder(type, userId)

            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(NOTIFICATION_INFO_KEY, holder)
        }
    }

}