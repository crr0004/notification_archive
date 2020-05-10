package dev.crrhodes.notificationarchive

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.IdlingResource
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), ServiceConnection {

    private var idlingResource: AdapterIdleResource? = null
    private var notificationService: NotificationService.NotificationBinder? = null
    private var adapter: NotificationListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = NotificationListAdapter(emptyList())
        this.notificationList.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        bindService(Intent(this, NotificationService::class.java), this, Context.BIND_IMPORTANT)
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

        this.lifecycleScope.launch {
            notificationService!!.getNotifications().collect {
                this@MainActivity.adapter?.setData(it)
                idlingResource?.setIdleState(true)
            }
        }
    }
    
    @VisibleForTesting
    fun getIdlingResource() : IdlingResource{
        if (idlingResource == null) {
            idlingResource = AdapterIdleResource();
        }
        return idlingResource!!
    }
}
