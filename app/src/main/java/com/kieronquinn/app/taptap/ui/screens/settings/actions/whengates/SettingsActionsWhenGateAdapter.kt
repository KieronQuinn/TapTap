package com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.coroutineScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ItemSettingsActionsWhenGateBinding
import com.kieronquinn.app.taptap.ui.screens.settings.actions.whengates.SettingsActionsWhenGatesViewModel.SettingsWhenGatesItem
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.onLongClicked
import com.kieronquinn.monetcompat.core.MonetCompat

class SettingsActionsWhenGatesAdapter(
    recyclerView: LifecycleAwareRecyclerView,
    private val onItemSelectedStateChange: (selected: Boolean) -> Unit,
    var items: List<SettingsWhenGatesItem>
) : LifecycleAwareRecyclerView.Adapter<SettingsActionsWhenGatesAdapter.ViewHolder>(recyclerView) {

    init {
        setHasStableIds(true)
    }

    private val context = recyclerView.context

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long {
        return items[position].whenGate.id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSettingsActionsWhenGateBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    private fun clearSelection() {
        items.forEachIndexed { index, item ->
            item.let {
                if (it.isSelected) {
                    it.isSelected = false
                    notifyItemChanged(index)
                }
            }
        }
    }

    fun getSelectedItemId(): Int? {
        return items.firstOrNull { it.isSelected }?.whenGate?.id
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.setup(items[position], holder)
    }

    private fun ItemSettingsActionsWhenGateBinding.setup(item: SettingsWhenGatesItem, holder: ViewHolder) = with(root) {
        applyBackgroundTint(monet)
        val gate = item.whenGate
        val gateLabel = if (gate.inverted) {
            context.getString(
                R.string.action_when_gate_inverted,
                context.getString(gate.gate.gate.nameRes)
            )
        } else {
            context.getString(gate.gate.gate.nameRes)
        }
        itemSettingsGateTitle.text = gateLabel
        itemSettingsGateContent.text = item.whenGate.gate.description
        itemSettingsGateIcon.setImageResource(gate.gate.gate.iconRes)
        holder.lifecycle.coroutineScope.launchWhenResumed {
            root.onLongClicked().collect {
                val isSelected = item.isSelected
                clearSelection()
                item.isSelected = !isSelected
                onItemSelectedStateChange(item.isSelected)
                notifyItemChanged(holder.adapterPosition)
            }
        }
    }

    data class ViewHolder(val binding: ItemSettingsActionsWhenGateBinding) :
        LifecycleAwareRecyclerView.ViewHolder(binding.root) {

        fun onRowSelectionChange(isSelected: Boolean) {
            binding.root.alpha = if (isSelected) 0.5f else 1f
        }

    }

}