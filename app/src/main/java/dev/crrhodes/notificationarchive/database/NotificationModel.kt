package dev.crrhodes.notificationarchive.database

import android.app.Notification
import android.content.pm.ApplicationInfo
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
            val contentsJsonObject = JSONObject()
            for(key: String in extras.keySet()){
                if(key == "android.appInfo") {
                    val appInfo = extras["android.appInfo"] as ApplicationInfo?
                    contentsJsonObject.put("android.packageName", appInfo?.packageName)
                }else {
                    contentsJsonObject.put(key, extras[key].toString())
                }

            }
            contentString = contentsJsonObject.toString()
        }
    }
}