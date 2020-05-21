package dev.crrhodes.notificationarchive

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.test.espresso.IdlingResource
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ServiceConnection, NotificationListAdapter.NotificationListActions {

    private var idlingResource: AdapterIdleResource? = null
    private var notificationService: NotificationService.NotificationBinder? = null
    private var adapter: NotificationListAdapter? = null
    private val model: NotificationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inflate our layout, views and bind our adapters.

        adapter = NotificationListAdapter(emptyList(), this)
        this.notificationList.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        // The actual binding for this happens in [onServiceConnected]
        bindService(Intent(this, NotificationService::class.java), this, Context.BIND_IMPORTANT)

        // This will persist as long as this application is alive
        this.lifecycleScope.launch {
            model.getNotifications().collect {
                // Every time the database gets updated, this will fire with a new list
                this@MainActivity.adapter?.setData(it)
                idlingResource?.setIdleState(true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        notificationService = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        this.notificationService = service as NotificationService.NotificationBinder


    }
    
    @VisibleForTesting
    fun getIdlingResource() : IdlingResource{
        // We use this for testing to ensure the activity is done doing anything
        if (idlingResource == null) {
            idlingResource = AdapterIdleResource("main activity");
        }
        return idlingResource!!
    }

    /**
     * Causes the [dev.crrhodes.notificationarchive.database.AppDatabase] to delete a notification
     * through [NotificationViewModel]. This is eventually consistent and will happen eventually
     * after this call.
     */
    override fun delete(notificationModel: NotificationModel) {
       model.delete(notificationModel)
    }

    /**
     * [MainActivity.snooze] is responsible for receiving notifications from the [NotificationListAdapter]
     * to "snooze". Snooze is telling the android system to play the notification at a later time.
     * Currently this snoozes the notification for an hour from [R.integer.snooze_time].
     */
    override fun snooze(item: NotificationModel) {
        val snoozeIntent = Intent(this, SnoozedNotificationsReceiver::class.java).let {
//            it.action = getString(R.string.snooze_action)
            it.putExtra("id", item.id)
            it.putExtra("content", item.contentString)
            PendingIntent.getBroadcast(this, 0, it, 0)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + resources.getInteger(R.integer.snooze_time),
            snoozeIntent
        )
        Toast.makeText(this, "Notification snoozed for an hour", Toast.LENGTH_SHORT).show()


    }
}
