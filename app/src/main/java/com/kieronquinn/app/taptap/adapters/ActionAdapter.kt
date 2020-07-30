package com.kieronquinn.app.taptap.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.ActionDataTypes
import kotlinx.android.synthetic.main.item_action.view.*

class ActionAdapter(private val context: Context, val actions: MutableList<ActionInternal>, private val isAdd: Boolean = false, private val onItemTouchListener: (ViewHolder) -> Unit) : RecyclerView.Adapter<ActionAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.item_action, parent, false))
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    fun moveItem(from: Int, to: Int) {
        val fromAction = actions[from]
        actions.removeAt(from)
        if (to < from) {
            actions.add(to, fromAction)
        } else {
            actions.add(to - 1, fromAction)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = actions[position]
        holder.itemView.apply {
            item_action_name.text = context.getString(item.action.nameRes)
            if(item.action.formattableDescription != null && item.data != null){
                item_action_description.text = getFormattedDescriptionForAction(item) ?: context.getString(item.action.descriptionRes)
            }else{
                item_action_description.text = context.getString(item.action.descriptionRes)
            }
            item_action_icon.setImageResource(item.action.iconRes)
            if(isAdd){
                item_action_handle.setImageResource(R.drawable.ic_add)
                setOnClickListener {
                    onItemTouchListener.invoke(holder)
                }
            }else {
                item_action_handle.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        onItemTouchListener.invoke(holder)
                    }
                    return@setOnTouchListener true
                }
            }
        }
    }

    private fun getFormattedDescriptionForAction(item: ActionInternal): CharSequence? {
        val formattedText = when(item.action.dataType){
            ActionDataTypes.PACKAGE_NAME -> {
                val applicationInfo = context.packageManager.getApplicationInfo(item.data, 0)
                applicationInfo.loadLabel(context.packageManager)
            }
            else -> null
        } ?: return null
        return context.getString(item.action.formattableDescription!!, formattedText)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}