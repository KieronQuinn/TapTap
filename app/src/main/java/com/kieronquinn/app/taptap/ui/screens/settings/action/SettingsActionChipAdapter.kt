package com.kieronquinn.app.taptap.ui.screens.settings.action

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.ItemActionChipAddBinding
import com.kieronquinn.app.taptap.databinding.ItemActionChipBinding
import com.kieronquinn.app.taptap.models.WhenGateInternal

class SettingsActionChipAdapter(context: Context, val viewModel: SettingsActionViewModel, var gates: MutableList<WhenGateInternal>, private val addListener: () -> Unit): RecyclerView.Adapter<SettingsActionChipAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount(): Int = gates.size + 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = when(holder) {
        is ViewHolder.SettingsActionChipItem -> {
            val item = gates[position]
            with(holder.itemActionChipBinding.root){
                text = item.getChipText(context)
                setChipIconResource(item.gate.iconRes)
                setOnCloseIconClickListener {
                    gates.removeAt(position)
                    notifyItemRemoved(position)
                    viewModel.commitChanges()
                }
            }
        }
        is ViewHolder.SettingsActionChipAdd -> {
            holder.itemActionChipAddBinding.root.setOnClickListener {
                addListener.invoke()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(ItemType.values()[viewType]){
            ItemType.ITEM -> ViewHolder.SettingsActionChipItem(ItemActionChipBinding.inflate(layoutInflater, parent, false))
            ItemType.ADD -> ViewHolder.SettingsActionChipAdd(ItemActionChipAddBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position < gates.size) ItemType.ITEM.ordinal
        else ItemType.ADD.ordinal
    }

    sealed class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        data class SettingsActionChipItem(val itemActionChipBinding: ItemActionChipBinding): ViewHolder(itemActionChipBinding.root)
        data class SettingsActionChipAdd(val itemActionChipAddBinding: ItemActionChipAddBinding): ViewHolder(itemActionChipAddBinding.root)
    }

    enum class ItemType {
        ITEM, ADD
    }
}