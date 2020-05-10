package dev.crrhodes.notificationarchive

import androidx.test.espresso.IdlingResource
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class AdapterIdleResource : IdlingResource{
    private var callback: IdlingResource.ResourceCallback? = null

    // Do this for thread safety. Tests operate in another thread
    private val isIdle = AtomicBoolean(false);
    private val name = UUID.randomUUID().toString()

    override fun getName(): String {
        return "AdapterIdlingResource: $name"
    }

    override fun isIdleNow(): Boolean {
        return isIdle.get()
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    fun setIdleState(isIdleNow: Boolean) {
        isIdle.set(isIdleNow);
        callback?.onTransitionToIdle()
    }
}