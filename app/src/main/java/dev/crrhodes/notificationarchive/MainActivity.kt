package dev.crrhodes.notificationarchive

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.test.espresso.IdlingResource
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ServiceConnection, NotificationListAdapter.NotificationListActions {

    private var snoozeTime: Int = 0
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
        val bindResult = bindService(Intent(this, NotificationService::class.java), this, Context.BIND_IMPORTANT)
        if(!bindResult){
            Toast.makeText(this, "Something went wrong with binding the service", Toast.LENGTH_SHORT).show()
        }

        // Check we have notification permission and ask the user to set in settings otherwise
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted){
                AlertDialog.Builder(this)
                    .setMessage(R.string.permission_not_granted_message)
                    .setPositiveButton("Launch") { _, _ ->
                        startActivity(
                            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        )
                    }
                    .setNegativeButton("Close"){ _, _ ->
                        finish()
                    }
                    .create().show()
            }
        }


        // This will persist as long as this application is alive
        this.lifecycleScope.launch {
            model.getNotifications().collect {
                // Every time the database gets updated, this will fire with a new list
                this@MainActivity.adapter?.setData(it)
                idlingResource?.setIdleState(true)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // Fire this on resume so we don't have to listen for the changes
        // We handle the default value in the let function rather than the getString
        snoozeTime = PreferenceManager.getDefaultSharedPreferences(this).getString(
            "snooze", null
        ).let {
            it?.toInt() ?: resources.getInteger(R.integer.snooze_time)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.main_settings_option ->{
                // Launch the settings activity when the user clicks the option
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
     * Currently this snoozes the notification for a time specified in the preference [R.xml.root_preferences].
     * The default time is specified in [R.integer.snooze_time].
     */
    override fun snooze(item: NotificationModel) {
        val snoozeIntent = Intent(this, SnoozedNotificationsReceiver::class.java).let {
            it.action = getString(R.string.snooze_action)
            it.putExtra("id", item.id)
            it.putExtra("content", item.contentString)
            PendingIntent.getBroadcast(this, 0, it, 0)
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            // 60 seconds to a minute (snoozeTime); 1000 milliseconds to a second
            System.currentTimeMillis() + (snoozeTime * 60 * 1000),
            snoozeIntent
        )
        Toast.makeText(this, "Notification snoozed for $snoozeTime minute(s)", Toast.LENGTH_SHORT).show()


    }
}
