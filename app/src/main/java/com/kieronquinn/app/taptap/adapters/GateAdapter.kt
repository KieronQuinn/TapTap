package com.kieronquinn.app.taptap.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.ActionDataTypes
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.GateDataTypes
import com.kieronquinn.app.taptap.models.GateInternal
import kotlinx.android.synthetic.main.item_gate.view.*

class GateAdapter(private val context: Context, private val items: MutableList<GateInternal>, private val isAdd: Boolean = false, private val listener: GateCallback) : RecyclerView.Adapter<GateAdapter.ViewHolder>() {

    private var currentlySelectedPosition = -1

    val isItemSelected
        get() = currentlySelectedPosition != -1

    interface GateCallback {
        fun onGateChange(gates: List<GateInternal>)
        fun onGateSelected()
        fun onGateDeselected()
    }

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.item_gate, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.itemView as CardView
        holder.itemView.apply {
            if(holder.adapterPosition == currentlySelectedPosition){
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_selected))
            }else{
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
            }
            item_gate_name.text = context.getString(item.gate.nameRes)
            if(item.gate.formattableDescription != null && item.data != null){
                item_gate_description.text = getFormattedDescriptionForGate(item) ?: context.getString(item.gate.descriptionRes)
            }else{
                item_gate_description.text = context.getString(item.gate.descriptionRes)
            }
            item_gate_icon.setImageResource(item.gate.iconRes)
            if(isAdd){
                item_gate_switch.visibility = View.INVISIBLE
                item_gate_switch.isClickable = false
                item_gate_switch.isFocusable = false
                item_gate_add.visibility = View.VISIBLE
                setOnClickListener {
                    listener.onGateChange(listOf(items[holder.adapterPosition]))
                }
            }else {
                item_gate_switch.isChecked = item.isActivated
                item_gate_switch.setOnCheckedChangeListener { _, isChecked ->
                    item.isActivated = isChecked
                    listener.onGateChange(items)
                }
                setOnClickListener {
                    item_gate_switch.toggle()
                }
                setOnLongClickListener {
                    setItemSelected(holder.adapterPosition)
                    true
                }
            }
        }
    }

    private fun setItemSelected(position: Int){
        val lastPosition = currentlySelectedPosition
        if(position == currentlySelectedPosition){
            //Deselect
            currentlySelectedPosition = -1
            notifyItemChanged(position)
            listener.onGateDeselected()
            return
        }
        currentlySelectedPosition = position
        if(lastPosition != -1){
            notifyItemChanged(lastPosition)
        }else{
            listener.onGateSelected()
        }
        notifyItemChanged(currentlySelectedPosition)
    }

    fun onDeleteClicked() {
        items.removeAt(currentlySelectedPosition)
        notifyItemRemoved(currentlySelectedPosition)
        currentlySelectedPosition = -1
        listener.onGateDeselected()
        listener.onGateChange(items)
    }

    fun deselectItem() {
        setItemSelected(-1)
        listener.onGateDeselected()
    }

    private fun getFormattedDescriptionForGate(item: GateInternal): CharSequence? {
        val formattedText = when(item.gate.dataType){
            GateDataTypes.PACKAGE_NAME -> {
                val applicationInfo = context.packageManager.getApplicationInfo(item.data, 0)
                applicationInfo.loadLabel(context.packageManager)
            }
            else -> null
        } ?: return null
        return context.getString(item.gate.formattableDescription!!, formattedText)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}