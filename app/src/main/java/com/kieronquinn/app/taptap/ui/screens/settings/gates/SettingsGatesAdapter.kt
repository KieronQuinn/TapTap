package com.kieronquinn.app.taptap.ui.screens.settings.gates

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ItemSettingsGatesGateBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsGatesInfoBinding
import com.kieronquinn.app.taptap.ui.screens.settings.gates.SettingsGatesViewModel.SettingsGatesItem
import com.kieronquinn.app.taptap.ui.screens.settings.gates.SettingsGatesViewModel.SettingsGatesItem.SettingsGatesItemType
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.views.applyMonetLight
import java.util.*

class SettingsGatesAdapter(
    recyclerView: LifecycleAwareRecyclerView,
    var items: ArrayList<SettingsGatesItem>,
    private val onHandleLongPressed: (ViewHolder) -> Unit,
    private val onItemSelectedStateChange: (selected: Boolean) -> Unit,
    private val onItemStateChanged: (id: Int, enabled: Boolean) -> Unit
) : LifecycleAwareRecyclerView.Adapter<SettingsGatesAdapter.ViewHolder>(recyclerView) {

    private val layoutInflater by lazy {
        recyclerView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (SettingsGatesItemType.values()[viewType]) {
            SettingsGatesItemType.GATE -> ViewHolder.Gate(
                ItemSettingsGatesGateBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            SettingsGatesItemType.HEADER -> ViewHolder.Header(
                ItemSettingsGatesInfoBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Gate -> holder.binding.setup(
                items[position] as SettingsGatesItem.Gate,
                holder
            )
            is ViewHolder.Header -> holder.binding.setup(
                items[position] as SettingsGatesItem.Header,
                holder.lifecycle
            )
        }
    }

    private fun ItemSettingsGatesGateBinding.setup(
        item: SettingsGatesItem.Gate,
        holder: ViewHolder
    ) {
        val context = root.context
        val tapGate = item.gate.gate
        root.backgroundTintList = if (!item.isSelected) {
            val fallbackBackground =
                if (context.isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
            ColorStateList.valueOf(
                monet.getBackgroundColorSecondary(context) ?: ContextCompat.getColor(
                    context,
                    fallbackBackground
                )
            )
        } else {
            ColorStateList.valueOf(monet.getPrimaryColor(context))
        }
        root.cardElevation = if (item.isSelected) {
            context.resources.getDimension(R.dimen.card_elevation_selected)
        } else 0f
        itemSettingsGatesGateTitle.text = context.getText(tapGate.nameRes)
        itemSettingsGatesGateIcon.setImageResource(tapGate.iconRes)
        itemSettingsGatesGateContent.text = item.gate.description
        itemSettingsGatesGateSwitch.isChecked = item.gate.enabled
        itemSettingsGatesGateSwitch.applyMonetLight()
        holder.lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsGatesGateSwitch.onClicked().collect {
                item.gate.enabled = !item.gate.enabled
                onItemStateChanged(item.gate.id, item.gate.enabled)
                notifyItemChanged(holder.adapterPosition)
            }
        }
        holder.lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsGatesGateHandle.onLongClicked().collect {
                onHandleLongPressed(holder)
            }
        }
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

    private fun ItemSettingsGatesInfoBinding.setup(item: SettingsGatesItem.Header, lifecycle: Lifecycle) {
        val context = root.context
        root.applyBackgroundTint(monet)
        itemSettingsGatesInfoContent.text = context.getText(item.contentRes)
        if(item.onClick != null){
            root.addRippleForeground()
            lifecycle.coroutineScope.launchWhenResumed {
                root.onClicked().collect {
                    item.onClick.invoke()
                }
            }
        }
        itemSettingsGatesInfoDismiss.isVisible = item.onCloseClick != null
        lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsGatesInfoDismiss.onClicked().collect {
                item.onCloseClick?.invoke()
            }
        }
    }

    fun clearSelection() {
        items.forEachIndexed { index, settingsGatesItem ->
            (settingsGatesItem as? SettingsGatesItem.Gate)?.let {
                if (it.isSelected) {
                    it.isSelected = false
                    notifyItemChanged(index)
                }
            }
        }
    }

    fun addItem(item: SettingsGatesItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeSelectedItem(): Int? {
        val selectedItemIndex = items.indexOfFirst { it is SettingsGatesItem.Gate && it.isSelected }
        val selectedItem = items[selectedItemIndex] as SettingsGatesItem.Gate
        if (selectedItemIndex == -1) return null
        items.removeAt(selectedItemIndex)
        notifyItemRemoved(selectedItemIndex)
        return selectedItem.gate.id
    }

    fun moveItem(indexFrom: Int, indexTo: Int): Boolean {
        if (indexFrom < indexTo) {
            for (i in indexFrom until indexTo) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in indexFrom downTo indexTo + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(indexFrom, indexTo)
        return true
    }

    sealed class ViewHolder(open val binding: ViewBinding) :
        LifecycleAwareRecyclerView.ViewHolder(binding.root) {
        data class Gate(override val binding: ItemSettingsGatesGateBinding) : ViewHolder(binding)
        data class Header(override val binding: ItemSettingsGatesInfoBinding) : ViewHolder(binding)

        fun onRowSelectionChange(isSelected: Boolean) {
            binding.root.alpha = if (isSelected) 0.5f else 1f
        }

    }

}