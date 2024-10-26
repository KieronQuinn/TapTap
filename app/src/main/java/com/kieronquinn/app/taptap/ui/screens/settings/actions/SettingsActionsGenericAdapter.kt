package com.kieronquinn.app.taptap.ui.screens.settings.actions

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ItemSettingsActionsActionBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsActionsInfoBinding
import com.kieronquinn.app.taptap.models.action.ActionRequirement
import com.kieronquinn.app.taptap.models.action.TapTapUIAction
import com.kieronquinn.app.taptap.ui.screens.settings.actions.SettingsActionsGenericViewModel.SettingsActionsItem
import com.kieronquinn.app.taptap.ui.screens.settings.actions.SettingsActionsGenericViewModel.SettingsActionsItem.SettingsActionsItemType
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.addRippleForeground
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.isDarkMode
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.onLongClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.monetcompat.core.MonetCompat
import java.util.Collections

class SettingsActionsGenericAdapter(
    recyclerView: RecyclerView,
    var items: ArrayList<SettingsActionsItem>,
    private val onHandleLongPressed: (ViewHolder) -> Unit,
    private val onItemSelectedStateChange: (selected: Boolean) -> Unit,
    private val onWhenGateChipClicked: (action: TapTapUIAction) -> Unit
) : LifecycleAwareRecyclerView.Adapter<SettingsActionsGenericAdapter.ViewHolder>(recyclerView) {

    private val layoutInflater by lazy {
        recyclerView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    private val chipBackground by lazy {
        ColorStateList.valueOf(
            monet.getBackgroundColorSecondary(recyclerView.context) ?:
            monet.getBackgroundColor(recyclerView.context)
        )
    }

    private val chipBackgroundPrimary by lazy {
        ColorStateList.valueOf(monet.getSecondaryColor(recyclerView.context))
    }

    private val googleSansTextMedium by lazy {
        ResourcesCompat.getFont(recyclerView.context, R.font.google_sans_text_medium)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (SettingsActionsItemType.values()[viewType]) {
            SettingsActionsItemType.ACTION -> ViewHolder.Action(
                ItemSettingsActionsActionBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            SettingsActionsItemType.HEADER -> ViewHolder.Header(
                ItemSettingsActionsInfoBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Action -> holder.binding.setup(
                items[position] as SettingsActionsItem.Action,
                holder
            )
            is ViewHolder.Header -> holder.binding.setup(
                items[position] as SettingsActionsItem.Header,
                holder.lifecycle
            )
        }
    }

    private fun ItemSettingsActionsActionBinding.setup(
        item: SettingsActionsItem.Action,
        holder: ViewHolder
    ) {
        val context = root.context
        val tapAction = item.action.tapAction
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
        itemSettingsActionsActionTitle.text = context.getText(tapAction.nameRes)
        itemSettingsActionsActionIcon.setImageResource(tapAction.iconRes)
        itemSettingsActionsActionContent.text = item.action.description
        val requirements = tapAction.actionRequirement
        requirements?.firstOrNull { it is ActionRequirement.UserDisplayedActionRequirement }
            ?.let { requirement ->
                requirement as ActionRequirement.UserDisplayedActionRequirement
                barrierTopOrChip.margin = context.resources.getDimension(R.dimen.margin_8).toInt()
                itemSettingsActionsActionChip.isVisible = true
                itemSettingsActionsActionChip.run {
                    text = context.getString(requirement.label)
                    typeface = googleSansTextMedium
                    chipBackgroundColor = chipBackground
                    chipIcon = ContextCompat.getDrawable(context, requirement.icon)
                }
            } ?: run {
            barrierTopOrChip.margin = 0
            itemSettingsActionsActionChip.isVisible = false
        }
        val whenGatesSize = item.action.whenGatesSize
        itemActionChipWhen.run {
            typeface = googleSansTextMedium
            chipBackgroundColor = chipBackgroundPrimary
        }
        if (whenGatesSize > 0) {
            itemActionChipWhen.chipIcon =
                ContextCompat.getDrawable(context, R.drawable.ic_action_chip_when_normal)
            itemActionChipWhen.text =
                context.resources.getQuantityString(
                    R.plurals.item_action_when_not_empty,
                    whenGatesSize,
                    whenGatesSize
                )
        } else {
            itemActionChipWhen.chipIcon =
                ContextCompat.getDrawable(context, R.drawable.ic_action_chip_when_empty)
            itemActionChipWhen.text = context.getString(R.string.item_action_when_empty)
        }
        holder.lifecycle.whenResumed {
            itemSettingsActionsActionHandle.onLongClicked().collect {
                onHandleLongPressed(holder)
            }
        }
        holder.lifecycle.whenResumed {
            root.onLongClicked().collect {
                val isSelected = item.isSelected
                clearSelection()
                item.isSelected = !isSelected
                onItemSelectedStateChange(item.isSelected)
                notifyItemChanged(holder.adapterPosition)
            }
        }
        holder.lifecycle.whenResumed {
            itemActionChipWhen.onClicked().collect {
                onWhenGateChipClicked(item.action)
            }
        }
    }

    private fun ItemSettingsActionsInfoBinding.setup(item: SettingsActionsItem.Header, lifecycle: Lifecycle) {
        val context = root.context
        root.applyBackgroundTint(monet)
        itemSettingsActionsInfoContent.text = context.getText(item.contentRes)
        if(item.onClick != null){
            root.addRippleForeground()
            lifecycle.whenResumed {
                root.onClicked().collect {
                    item.onClick.invoke()
                }
            }
        }
        lifecycle.whenResumed {
            itemSettingsActionsInfoDismiss.onClicked().collect {
                item.onCloseClick.invoke()
            }
        }
    }

    fun clearSelection() {
        items.forEachIndexed { index, settingsActionsItem ->
            (settingsActionsItem as? SettingsActionsItem.Action)?.let {
                if (it.isSelected) {
                    it.isSelected = false
                    notifyItemChanged(index)
                }
            }
        }
    }

    fun addItem(item: SettingsActionsItem) {
        items.add(item)
        val index = items.size - 1
        if(index < 0) return
        notifyItemInserted(index)
    }

    fun updateWhenGatesSize(actionId: Int, size: Int) {
        val position =
            items.indexOfFirst { it is SettingsActionsItem.Action && it.action.id == actionId }
        if(position == -1) return
        (items[position] as? SettingsActionsItem.Action)?.action?.whenGatesSize = size
        notifyItemChanged(position)
    }

    fun removeSelectedItem(): Int? {
        val selectedItem = items.indexOfFirst { it is SettingsActionsItem.Action && it.isSelected }
        if (selectedItem == -1) return null
        val item = items[selectedItem] as SettingsActionsItem.Action
        items.removeAt(selectedItem)
        notifyItemRemoved(selectedItem)
        return item.action.id
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
        data class Action(override val binding: ItemSettingsActionsActionBinding) :
            ViewHolder(binding)

        data class Header(override val binding: ItemSettingsActionsInfoBinding) :
            ViewHolder(binding)

        fun onRowSelectionChange(isSelected: Boolean) {
            binding.root.alpha = if (isSelected) 0.5f else 1f
        }

    }

}