package dev.crrhodes.notificationarchive

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.crrhodes.notificationarchive.database.AppDatabase
import dev.crrhodes.notificationarchive.database.NotificationModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * [NotificationService] is responsible for listening to the [android.content.Context.NOTIFICATION_SERVICE]
 * for notifications being posted and then storing it into the [AppDatabase] using the [dev.crrhodes.notificationarchive.database.dao.NotificationDao].
 * The android manifest registers this service against the system to bind to the notification service.
 */
class NotificationService : NotificationListenerService() {
    private var listenerConnected: Boolean = false
    private lateinit var db: AppDatabase
    private lateinit var backgroundThread: ExecutorService

    override fun onCreate() {
        super.onCreate()
        backgroundThread =  Executors.newSingleThreadExecutor();

        db = AppDatabase.getDatabase(this)

    }

    override fun onListenerConnected() {
        this.listenerConnected = true
        super.onListenerConnected()
    }

    override fun onBind(intent: Intent?): IBinder? {
        /*
        We need to listen for intents from the system and intents from our activities wishing to bind
        against the service.
         */
        return if(intent?.action?.equals("android.service.notification.NotificationListenerService") == true){
            super.onBind(intent)
        }else {
            NotificationBinder(this)
        }
    }

    override fun onListenerDisconnected() {
        this.listenerConnected = false
        super.onListenerDisconnected()
    }

    /**
     * [onNotificationPosted] is called from the bound system each time a notification is posted
     * and we need to grab the notification and store it into our database.
     */
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // Don't store notifications that are replayed
        if(sbn?.notification != null && !sbn.notification.extras.containsKey("replay")) {
            backgroundThread.submit {
                db.notificationDao().insert(NotificationModel(sbn.notification))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundThread.shutdownNow()
        db.close()
    }

    open class NotificationBinder(private val service: NotificationService) : Binder(){
    }

}