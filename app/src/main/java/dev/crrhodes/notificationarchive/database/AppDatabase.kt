package dev.crrhodes.notificationarchive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.crrhodes.notificationarchive.database.dao.NotificationDao

@Database(entities = arrayOf(NotificationModel::class), version = 1)
@TypeConverters(dev.crrhodes.notificationarchive.database.TypeConverters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun notificationDao(): NotificationDao
    companion object {
        private var database: AppDatabase? = null
        var inMemory: Boolean = false

        fun getDatabase(context: Context) : AppDatabase{
            if(database == null){
                database = if(inMemory){
                    Room.inMemoryDatabaseBuilder(
                        context, AppDatabase::class.java
                    ).allowMainThreadQueries().build()

                }else {
                    Room.databaseBuilder(
                        context,
                        AppDatabase::class.java, "notification-db"
                    ).build()
                }
            }
            return database!!
        }
        fun closeDatabase(){
            database?.close()
            database = null
        }
    }
}