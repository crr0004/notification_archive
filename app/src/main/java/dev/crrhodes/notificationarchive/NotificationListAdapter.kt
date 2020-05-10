package dev.crrhodes.notificationarchive

import android.app.Notification
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.JsonReader
import android.util.JsonWriter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.android.synthetic.main.notification_list_item.view.*
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

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
        try{
            val notificationJson = JSONObject(JSONTokener(item.contentString))
            holder.itemView.notificationTitleTxt.text = notificationJson.getString("android.title")
            holder.itemView.notificationBodyTxt.text = notificationJson.getString("android.text")
            val icon: Drawable = holder.itemView.context.packageManager.getApplicationIcon(notificationJson.getString("android.packageName"))
            holder.itemView.notificationAppIcon.setImageDrawable(icon)
        }catch(e: JSONException){
            holder.itemView.notificationBodyTxt.text = holder.itemView.context.getString(R.string.notification_parse_error)
        }catch(e: PackageManager.NameNotFoundException){
            holder.itemView.notificationAppIcon.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }

    fun setData(data: List<NotificationModel>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    class NotificationListViewHolder(view: View) : RecyclerView.ViewHolder(view)
}