package dev.crrhodes.notificationarchive

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.crrhodes.notificationarchive.database.AppDatabase
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.*

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

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if(sbn?.notification != null) {
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
        fun getNotifications(): Flow<List<NotificationModel>> {
            return service.db.notificationDao().getAll()
        }

    }

}