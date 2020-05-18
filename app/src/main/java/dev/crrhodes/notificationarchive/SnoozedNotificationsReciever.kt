package dev.crrhodes.notificationarchive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class SnoozedNotificationsReciever() : BroadcastReceiver() {
    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "test_channel"
            val descriptionText = "test_channel_descr"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("test_channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if(context != null && intent != null) {
            createNotificationChannel(context)
            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, "test_channel")
            } else {
                Notification.Builder(context)
            }
            notification.setContentTitle("context_title")
            notification.setContentText("context_text")
            notification.setSmallIcon(R.drawable.ic_launcher_foreground)
            NotificationManagerCompat.from(context).notify(
                intent.getIntExtra("id", 0),
                notification.build()
            )

        }

    }
}