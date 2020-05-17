package dev.crrhodes.notificationarchive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.JsonReader
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dev.crrhodes.notificationarchive.database.AppDatabase
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.not
import org.json.JSONObject
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
    val testNotification = JSONObject()
    val adapterIdleResource = AdapterIdleResource("test idle")

    companion object {
        @BeforeClass
        @JvmStatic
        fun staticBefore() {
            // Really make sure the database doesn't exist. Tests can be a bit temperamental because the rule
            AppDatabase.closeDatabase()
            AppDatabase.inMemory = true
        }
    }

    @Before
    fun setup(){
        activityRule.scenario.onActivity {
            IdlingRegistry.getInstance().register(it.getIdlingResource())
            testNotification.put("android.title", "Test Title")
            testNotification.put("android.text", "Test text")
            testNotification.put("android.packageName", "")
            AppDatabase.getDatabase(it).notificationDao().insert(
                NotificationModel(0, testNotification.toString())
            )
        }
        IdlingRegistry.getInstance().register(adapterIdleResource)

        activityRule.scenario.onActivity {
            it.notificationList.adapter?.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
                override fun onChanged() {
                    super.onChanged()
                    adapterIdleResource.setIdleState(true)
                }
            })
        }
    }

    @After
    fun after(){
        activityRule.scenario.onActivity {
            IdlingRegistry.getInstance().unregister(it.getIdlingResource())
        }
        IdlingRegistry.getInstance().unregister(adapterIdleResource)

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
        onView(withText(testNotification.getString("android.title"))).check(matches(isDisplayed()))
        onView(withText(testNotification.getString("android.text"))).check(matches(isDisplayed()))
    }

    @Test
    fun notificationOptionsListShows(){
        onView(withText("Delete")).check(doesNotExist())
        onView(withId(R.id.moreVertNotificationItem)).perform(click())
        onView(withText("Delete")).check(matches(isDisplayed()))
        onView(withText("Snooze")).check(matches(isDisplayed()))
    }

    @Test
    fun canDeleteNotification(){
        onView(withId(R.id.moreVertNotificationItem)).perform(click())
        onView(withText("Delete")).perform(click())
        onView(withText(testNotification.getString("android.title"))).check(doesNotExist())
    }
}
