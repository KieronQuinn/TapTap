package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.appshortcuts

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.components.columbus.actions.custom.LaunchAppShortcutAction
import com.kieronquinn.app.taptap.databinding.ItemSettingsSharedAppShortcutsSelectorAppBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsSharedAppShortcutsSelectorShortcutBinding
import com.kieronquinn.app.taptap.models.columbus.AppShortcutData
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.appshortcuts.SettingsSharedAppShortcutsSelectorViewModel.Item
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.picasso.AppIconRequestHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collect

class SettingsSharedAppShortcutsSelectorAdapter(
    recyclerView: RecyclerView,
    var items: List<Item>,
    private val onAppClicked: (packageName: String) -> List<Int>,
    private val onAppShortcutClicked: (shortcut: AppShortcutData) -> Unit
) :
    LifecycleAwareRecyclerView.Adapter<SettingsSharedAppShortcutsSelectorAdapter.ViewHolder>(
        recyclerView
    ) {

    private val layoutInflater by lazy {
        recyclerView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val picasso = Picasso.get()

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].itemType.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (Item.ItemType.values()[viewType]) {
            Item.ItemType.APP -> ViewHolder.App(
                ItemSettingsSharedAppShortcutsSelectorAppBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            Item.ItemType.APP_SHORTCUT -> ViewHolder.AppShortcut(
                ItemSettingsSharedAppShortcutsSelectorShortcutBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is ViewHolder.App -> holder.binding.setup(item as Item.App, holder.lifecycle)
            is ViewHolder.AppShortcut -> holder.binding.setup(
                item as Item.AppShortcut,
                holder.lifecycle
            )
        }
    }

    private fun ItemSettingsSharedAppShortcutsSelectorAppBinding.setup(
        item: Item.App,
        lifecycle: Lifecycle
    ) {
        itemSettingsSharedAppShortcutsSelectorAppLabel.text = item.name
        val uri = Uri.parse("${AppIconRequestHandler.SCHEME_PNAME}:${item.packageName}")
        picasso.load(uri).into(itemSettingsSharedAppShortcutsSelectorAppIcon)
        itemSettingsSharedAppShortcutsSelectorAppChevron.rotation = if (item.isOpen) 180f else 0f
        lifecycle.coroutineScope.launchWhenResumed {
            root.onClicked().collect {
                toggleItem(item, itemSettingsSharedAppShortcutsSelectorAppChevron)
            }
        }
        lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsSharedAppShortcutsSelectorAppChevron.onClicked().collect {
                toggleItem(item, it)
            }
        }
    }

    private fun toggleItem(item: Item.App, view: View) {
        val itemsToUpdate = onAppClicked(item.packageName)
        if (itemsToUpdate.isNotEmpty()) {
            item.isOpen = !item.isOpen
            if (item.isOpen) {
                view.animate().rotation(180f).start()
            } else {
                view.animate().rotation(0f).start()
            }
        }
        itemsToUpdate.forEach { item ->
            notifyItemChanged(item)
        }
    }

    private fun ItemSettingsSharedAppShortcutsSelectorShortcutBinding.setup(
        item: Item.AppShortcut,
        lifecycle: Lifecycle
    ) {
        root.isVisible = item.isVisible
        root.updateLayoutParams<RecyclerView.LayoutParams> {
            height = if (item.isVisible) RecyclerView.LayoutParams.WRAP_CONTENT else 0
        }
        itemSettingsSharedAppShortcutsSelectorShortcutLabel.text = item.name
        if (item.icon.cacheIcon != null) {
            picasso.load(item.icon.cacheIcon).into(itemSettingsSharedAppShortcutsSelectorShortcutIcon)
        } else if (item.icon.icon != null) {
            itemSettingsSharedAppShortcutsSelectorShortcutIcon.setImageIcon(item.icon.icon)
        }
        lifecycle.coroutineScope.launchWhenResumed {
            root.onClicked().collect {
                val selectedAppShortcut = AppShortcutData(item.packageName, item.shortcutId, item.name.toString())
                onAppShortcutClicked(selectedAppShortcut)
            }
        }
    }

    sealed class ViewHolder(open val binding: ViewBinding) :
        LifecycleAwareRecyclerView.ViewHolder(binding.root) {
        data class App(override val binding: ItemSettingsSharedAppShortcutsSelectorAppBinding) :
            ViewHolder(binding)

        data class AppShortcut(override val binding: ItemSettingsSharedAppShortcutsSelectorShortcutBinding) :
            ViewHolder(binding)
    }

}