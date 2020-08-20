package com.kieronquinn.app.taptap.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.ActionDataTypes
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.utils.deserialize
import kotlinx.android.synthetic.main.item_action.view.*

class ActionAdapter(private val context: Context, val actions: MutableList<ActionInternal>, private val isAdd: Boolean = false, private val onItemTouchListener: (ViewHolder) -> Unit) : RecyclerView.Adapter<ActionAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    var chipAddCallback: ((Int, Array<TapGate>) -> Unit)? = null
    var saveCallback: (() -> Unit)? = null
    var chipClickCallback: ((TapGate) -> Unit)? = null

    private val observer = object: RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            super.onChanged()
            updateInfoPosition()
        }
    }

    private fun updateInfoPosition() : Int {
        return actions.find { it.isBlocking() }?.let {
            actions.indexOf(it)
        } ?: run {
            Integer.MAX_VALUE
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        registerAdapterDataObserver(observer)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        unregisterAdapterDataObserver(observer)
    }

    var currentInfoPosition : Int? = null
        get() {
            return if(field == null){
                updateInfoPosition()
            }else{
                field
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.item_action, parent, false))
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    fun moveItem(from: Int, to: Int, recyclerView: RecyclerView) {
        val fromAction = actions[from]
        actions.set(from, actions[to])
        actions.set(to, fromAction)
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
            if(!isAdd) {
                item_action_chips.run {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = ActionChipAdapter(context, item.whenList, chipClickCallback){ gates ->
                        chipAddCallback?.invoke(holder.adapterPosition, gates)
                    }.apply {
                        chipRemoveCallback = {
                            actions[holder.adapterPosition].whenList.removeAt(it)
                            notifyItemRemoved(it)
                            //Update Add... if needed
                            notifyItemChanged(itemCount - 1)
                            this@ActionAdapter.notifyItemChanged(holder.adapterPosition)
                            saveCallback?.invoke()
                        }
                    }
                }
                if (item.whenList.isEmpty()) {
                    holder.itemView.item_action_when.visibility = View.GONE
                } else {
                    holder.itemView.item_action_when.text = if(item.whenList.size > 1){
                        context.getString(R.string.item_action_when_multiple)
                    }else{
                        context.getString(R.string.item_action_when)
                    }
                    holder.itemView.item_action_when.visibility = View.VISIBLE
                }
                if(holder.adapterPosition == currentInfoPosition && holder.adapterPosition != itemCount - 1){
                    holder.itemView.item_action_blocked_info.visibility = View.VISIBLE
                }else{
                    holder.itemView.item_action_blocked_info.visibility = View.GONE
                }
            }else{
                item_action_when.visibility = View.GONE
                item_action_chips.visibility = View.GONE
            }
        }
    }

    private fun getFormattedDescriptionForAction(item: ActionInternal): CharSequence? {
        val formattedText = when(item.action.dataType){
            ActionDataTypes.PACKAGE_NAME -> {
                val applicationInfo = context.packageManager.getApplicationInfo(item.data, 0)
                applicationInfo.loadLabel(context.packageManager)
            }
            ActionDataTypes.SHORTCUT -> {
                val intent = Intent().apply {
                    deserialize(item.data ?: "")
                }
                try {
                    context.packageManager.queryIntentActivities(intent, 0).firstOrNull()?.let {
                        val applicationInfo = context.packageManager.getApplicationInfo(
                            it.activityInfo.packageName,
                            0
                        )
                        applicationInfo.loadLabel(context.packageManager)
                    } ?: run {
                        null
                    }
                }catch (e: Exception){
                    null
                }
            }
            else -> null
        } ?: return null
        return context.getString(item.action.formattableDescription!!, formattedText)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}