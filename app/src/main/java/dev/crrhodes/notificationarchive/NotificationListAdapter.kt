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


/**
 * [NotificationListAdapter] is responsible for managing the views associated with [NotificationMode].
 * You need to implement [NotificationListActions] for this to work, as the actions require a context.
 */
class NotificationListAdapter(private var data: List<NotificationModel>, private val actionHolder: NotificationListActions) :
    RecyclerView.Adapter<NotificationListAdapter.NotificationListViewHolder>() {

    /**
     * Inflates [R.layout.notification_list_item] for the views
     */
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
            // Set the view properties from the JSON object encoded in the content string
            // taking care to ensure any issues in the JSON don't cause the app to stop
            val notificationJson = JSONObject(JSONTokener(item.contentString))
            holder.itemView.notificationTitleTxt.text = notificationJson.getString("android.title")
            holder.itemView.notificationBodyTxt.text = notificationJson.getString("android.text")

            // Grab the app icon from the system, ensuring we have a default
            val icon: Drawable = holder.itemView.context.packageManager.getApplicationIcon(notificationJson.getString("android.packageName"))
            holder.itemView.notificationAppIcon.setImageDrawable(icon)
        }catch(e: JSONException){
            holder.itemView.notificationBodyTxt.text = holder.itemView.context.getString(R.string.notification_parse_error)
            holder.itemView.notificationAppIcon.setImageResource(R.drawable.ic_launcher_foreground)
        }catch(e: PackageManager.NameNotFoundException){
            // Couldn't find an icon for the package name
            holder.itemView.notificationAppIcon.setImageResource(R.drawable.ic_launcher_foreground)
        }
        // When the extra options button is clicked, pop-up a menu for more options
        // noting the actions are handled in elsewhere as we don't hold the app context here
        holder.itemView.moreVertNotificationItem.setOnClickListener {
            val popupMenu = PopupMenu(it.context, it)
            popupMenu.inflate(R.menu.notification_more_menu)
            popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
                when(menuItem.itemId){
                    R.id.delete_notification -> {
                        // This causes the view to fire an animation for removal
                        this@NotificationListAdapter.notifyItemRemoved(position)
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
        this.notifyDataSetChanged() // this just ensures the view actually changes
    }

    /**
     * Implement this interface to implement the actions that can be taken on the items.
     */
    interface NotificationListActions{
        fun delete(notificationModel: NotificationModel)
        fun snooze(item: NotificationModel)
    }
    class NotificationListViewHolder(view: View) : RecyclerView.ViewHolder(view)
}