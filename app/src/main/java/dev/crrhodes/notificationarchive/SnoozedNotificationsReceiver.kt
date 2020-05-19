package dev.crrhodes.notificationarchive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import org.json.JSONObject
import org.json.JSONTokener

/**
 * [SnoozedNotificationsReceiver] is responsible for listening for "snoozed" notifications
 * and replaying them as another notification. Currently "snoozes" the notification for an hour in
 * [MainActivity.snooze].
 */
class SnoozedNotificationsReceiver() : BroadcastReceiver() {
    /**
     * Creates a notification called "replay_channel" for "snoozed" notifications to be queued in.
     * This only works if the [Build.VERSION.SDK_INT] is greater or equal to [Build.VERSION_CODES.O].
     */
    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "replay_channel"
            val descriptionText = "Replay Snoozed Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("replay_channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * [onReceive] is responsible for receiving intents registered in AndroidManifest.xml.
     * Currently only supports receiving [R.string.snooze_action].
     * The intent requires a notification encoded as json in the key "content".
     * @see MainActivity.snooze
     */
    override fun onReceive(context: Context?, intent: Intent?) {

        if(context != null && intent != null && intent.action == context.getString(R.string.snooze_action)) {
            createNotificationChannel(context)
            val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, "replay_channel")
            } else {
                Notification.Builder(context)
            }
            notification.setContentText("context_text")
            notification.setSmallIcon(R.drawable.ic_launcher_foreground)
            val jsonObject = JSONObject(JSONTokener(intent.getStringExtra("content")))
            jsonObject.keys().forEach {
                notification.extras.putString(it, jsonObject.getString(it));
            }
            notification.extras.putBoolean("replay", true)

            if(notification.extras.containsKey("android.reduced.images")){
                notification.extras.putBoolean(
                    "android.reduced.images", notification.extras.getString("android.reduced.images", "")!!.toBoolean()
                )

            }

            notification.setContentText(jsonObject.getString("android.text"))
            notification.setContentTitle(jsonObject.getString("android.title"))

            NotificationManagerCompat.from(context).notify(
                intent.getIntExtra("id", 0),
                notification.build()
            )

        }

    }
}