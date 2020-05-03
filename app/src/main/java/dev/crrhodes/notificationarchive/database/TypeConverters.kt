package dev.crrhodes.notificationarchive.database

import android.app.Notification
import androidx.room.TypeConverter

class TypeConverters {
    @TypeConverter
    fun toNotificationModel(value: Notification): NotificationModel{
        return NotificationModel(0, value.toString())
    }
}