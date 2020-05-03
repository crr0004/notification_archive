package dev.crrhodes.notificationarchive

import android.app.Notification
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.android.synthetic.main.notification_list_item.view.*

class NotificationListAdapter(private var data: List<NotificationModel>) :
    RecyclerView.Adapter<NotificationListAdapter.NotificationListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_list_item, parent, false)
        return NotificationListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: NotificationListViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.notificationTitleTxt.text = item.contentString
    }

    fun setData(data: List<NotificationModel>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    class NotificationListViewHolder(view: View) : RecyclerView.ViewHolder(view)
}