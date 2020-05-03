package dev.crrhodes.notificationarchive.database

import android.app.Notification
import android.service.notification.StatusBarNotification
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "notifications")
data class NotificationModel (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "contents") var contentString: String
){
    constructor() : this(0, "")
    constructor(notification: Notification) : this() {
        val extras = notification.extras
        if(!extras.isEmpty){
            val contentStringBuilder = JSONObject()
            for(key: String in extras.keySet()){
                contentStringBuilder.put(key, extras[key].toString())
            }
            contentString = contentStringBuilder.toString()
        }
    }
}