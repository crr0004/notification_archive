package dev.crrhodes.notificationarchive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.crrhodes.notificationarchive.database.AppDatabase
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NotificationViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.getDatabase(app)
    fun getNotifications(): Flow<List<NotificationModel>> {
        return db.notificationDao().getAll()
    }

    override fun onCleared() {
        super.onCleared()
        db.close()
    }

    fun delete(notificationModel: NotificationModel) {
        this.viewModelScope.launch {
            db.notificationDao().delete(notificationModel)
        }
    }
}