package az.rabita.lifestep.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import az.rabita.lifestep.R

class LocalNotification : BroadcastReceiver() {

    companion object {
        const val CHANNEL_NAME = "az.rabita.lifestep.broadcast.LocalNotification"
        const val NOTIFICATION_ID = 5000
    }

    override fun onReceive(context: Context?, pending: Intent?) {
        context?.let {
            val builder = NotificationCompat.Builder(it, CHANNEL_NAME)
                .setSmallIcon(R.drawable.ic_stat_onesignal_default)
                .setContentTitle("Reminder Notification")
                .setContentText("Reminder notification at specific time")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val manager = NotificationManagerCompat.from(it)

            manager.notify(NOTIFICATION_ID, builder.build())
        }
    }

}