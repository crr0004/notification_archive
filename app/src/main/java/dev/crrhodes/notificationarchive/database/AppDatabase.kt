package dev.crrhodes.notificationarchive.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.crrhodes.notificationarchive.database.dao.NotificationDao

@Database(entities = arrayOf(NotificationModel::class), version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun notificationDao(): NotificationDao
    companion object {
        private var database: AppDatabase? = null
        var inMemory: Boolean = false

        fun getDatabase(context: Context) : AppDatabase{
            /*
             We need to take care if we are using a test database or the application version.
             This is a singleton method so the database is only created if no connection already
             exists.

             When using the test database we don't want to fiddle with threads, so just allow
             main thread queries as well.
             */
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