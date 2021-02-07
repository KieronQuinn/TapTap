package com.kieronquinn.app.taptap.ui.screens.settings.gate

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ItemGateBinding
import com.kieronquinn.app.taptap.databinding.ItemGateHeaderBinding
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.utils.extensions.observe

class SettingsGateAdapter(context: Context, private val viewModel: SettingsGateViewModel, lifecycleOwner: LifecycleOwner, var items: MutableList<GateInternal>, private val headerClick: () -> Unit): RecyclerView.Adapter<SettingsGateAdapter.ViewHolder>() {

    init {
        viewModel.selectedState.observe(lifecycleOwner){
            if(it.previousSelectedIndex != -1){
                notifyItemChanged(it.previousSelectedIndex)
            }
            if(it is SettingsGateViewModel.SelectedState.Selected){
                notifyItemChanged(it.selectedItemIndex)
            }
        }
    }

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount() = items.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(ItemType.values()[viewType]){
            ItemType.HEADER -> ViewHolder.SettingsGateItemHeader(ItemGateHeaderBinding.inflate(layoutInflater, parent, false))
            ItemType.GATE -> ViewHolder.SettingsGateItemGate(ItemGateBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == 0) ItemType.HEADER.ordinal
        else ItemType.GATE.ordinal
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding){
        when(this) {
            is ItemGateBinding -> {
                root.run {
                    if(holder.adapterPosition == viewModel.getSelectedIndex()){
                        setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_selected))
                    }else{
                        setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
                    }
                }
                val item = items[holder.adapterPosition - 1]
                itemGateIcon.setImageResource(item.gate.iconRes)
                itemGateName.setText(item.gate.nameRes)
                itemGateDescription.text = item.getCardDescription(itemGateDescription.context)
                itemGateSwitch.isChecked = item.isActivated
                itemGateSwitch.setOnClickListener {
                    viewModel.onItemCheckClicked(item)
                }
                root.setOnLongClickListener {
                    viewModel.setSelectedIndex(holder.adapterPosition)
                    true
                }
            }
            is ItemGateHeaderBinding -> {
                root.setOnClickListener {
                    headerClick.invoke()
                }
            }
            else -> {}
        }
    }

    sealed class ViewHolder(open val binding: ViewBinding): RecyclerView.ViewHolder(binding.root){
        data class SettingsGateItemHeader(override val binding: ItemGateHeaderBinding): ViewHolder(binding)
        data class SettingsGateItemGate(override val binding: ItemGateBinding): ViewHolder(binding)
    }

    private enum class ItemType {
        HEADER, GATE
    }
}