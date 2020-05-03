package dev.crrhodes.notificationarchive.database.dao

import android.app.Notification
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications")
    fun getAll(): Flow<List<NotificationModel>>

    @Insert
    fun insert(notification: NotificationModel)

}