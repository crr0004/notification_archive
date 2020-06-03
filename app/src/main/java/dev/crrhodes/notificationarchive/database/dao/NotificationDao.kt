package dev.crrhodes.notificationarchive.database.dao

import android.app.Notification
import androidx.room.*
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications")
    fun getAll(): Flow<List<NotificationModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(notification: NotificationModel)

    @Delete
    suspend fun delete(notification: NotificationModel)

}