package com.kieronquinn.app.taptap.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.utils.getFormattedDataForGate

class ActionChipAdapter(private val context: Context, private val items: ArrayList<WhenGateInternal>, private val chipClickCallback: ((TapGate) -> Unit)?, private val chipAddCallback: ((Array<TapGate>) -> Unit)) : RecyclerView.Adapter<ActionChipAdapter.ViewHolder>() {

    enum class ItemType {
        CHIP,
        FOOTER
    }

    var chipRemoveCallback: ((Int) -> Unit)? = null

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType){
            ItemType.CHIP.ordinal -> ViewHolder(layoutInflater.inflate(R.layout.item_action_chip, parent, false))
            ItemType.FOOTER.ordinal -> ViewHolder(layoutInflater.inflate(R.layout.item_action_chip_add, parent, false))
            else -> ViewHolder(View(context))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position > items.size - 1) ItemType.FOOTER.ordinal
        else ItemType.CHIP.ordinal
    }

    override fun getItemCount(): Int {
        //Include "add"
        return items.size + 1
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = getItemViewType(position)
        val chip = holder.itemView as Chip
        if(viewType == ItemType.CHIP.ordinal){
            val whenGate = items[position]
            chip.text = if(whenGate.gate.formattableDescription != null && whenGate.gate.dataType != null){
                val gateTitle = context.getString(whenGate.gate.nameRes)
                val formattedText = getFormattedDataForGate(context, whenGate.gate, whenGate.data)
                "$gateTitle ($formattedText)"
            }else context.getString(whenGate.gate.nameRes)
            chip.setChipIconResource(whenGate.gate.iconRes)
            chip.setOnCloseIconClickListener {
                chipRemoveCallback?.invoke(holder.adapterPosition)
            }
            chip.setOnClickListener {
                chipClickCallback?.invoke(whenGate.gate)
            }
        }else{
            chip.setOnClickListener {
                chipAddCallback.invoke(items.map { it.gate }.toTypedArray())
            }
            if(items.isEmpty()){
                chip.text = context.getString(R.string.item_action_when_add_empty)
            }else{
                chip.text = context.getString(R.string.item_action_when_add)
            }
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}