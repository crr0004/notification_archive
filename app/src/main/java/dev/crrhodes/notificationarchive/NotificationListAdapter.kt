package dev.crrhodes.notificationarchive

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import dev.crrhodes.notificationarchive.database.NotificationModel
import kotlinx.android.synthetic.main.notification_list_item.view.*
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class NotificationListAdapter(private var data: List<NotificationModel>, private val actionHolder: NotificationListActions) :
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
        holder.itemView.moreVertNotificationItem.setOnClickListener {
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.inflate(R.menu.notification_more_menu)
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when(menuItem.itemId){
                    R.id.delete_notification -> {
                        actionHolder.delete(item)
                        true
                    }
                    R.id.snooze_notification -> {
                        actionHolder.snooze(item)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

    }

    fun setData(data: List<NotificationModel>) {
        this.data = data
        this.notifyDataSetChanged()
    }

    interface NotificationListActions{
        fun delete(notificationModel: NotificationModel)
        fun snooze(item: NotificationModel)
    }
    class NotificationListViewHolder(view: View) : RecyclerView.ViewHolder(view)
}