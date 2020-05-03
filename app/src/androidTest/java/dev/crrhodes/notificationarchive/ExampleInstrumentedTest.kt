package dev.crrhodes.notificationarchive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dev.crrhodes.notificationarchive.database.AppDatabase
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking
import org.junit.*


import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ExampleInstrumentedTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    companion object {
        @BeforeClass
        @JvmStatic
        fun staticBefore() {
            AppDatabase.inMemory = true
        }
    }

    @Before
    fun setup(){
        activityRule.scenario.onActivity {
            IdlingRegistry.getInstance().register(it.getIdlingResource())
            AppDatabase.getDatabase(it).notificationDao().insert(
                NotificationModel(0, "test_notification")
            )
        }
    }

    @After
    fun after(){
        activityRule.scenario.onActivity {
            IdlingRegistry.getInstance().unregister(it.getIdlingResource())
        }

    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("dev.crrhodes.notificationarchive", appContext.packageName)
    }

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

    @Test
    fun notificationIsShownInList(){
        val adapterIdleResource = AdapterIdleResource()
        IdlingRegistry.getInstance().register(adapterIdleResource)

        activityRule.scenario.onActivity {
            it.notificationList.adapter?.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                override fun onChanged() {
                    super.onChanged()
                    adapterIdleResource.setIdleState(true)
                }
            })
        }
        onView(withText("test_notification")).check(matches(isDisplayed()))
        IdlingRegistry.getInstance().unregister(adapterIdleResource)
    }
}
