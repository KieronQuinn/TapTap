package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.shortcuts

import android.content.ComponentName
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
import com.kieronquinn.app.taptap.databinding.ItemSettingsSharedShortcutsSelectorAppBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsSharedShortcutsSelectorShortcutBinding
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.shortcuts.SettingsSharedShortcutsSelectorViewModel.Item
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.picasso.AppIconRequestHandler
import com.kieronquinn.app.taptap.utils.picasso.ComponentNameIconRequestHandler
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.collect

class SettingsSharedShortcutsSelectorAdapter(
    recyclerView: RecyclerView,
    var items: List<Item>,
    private val onAppClicked: (packageName: String) -> List<Int>,
    private val onShortcutClicked: (componentName: ComponentName) -> Unit
) :
    LifecycleAwareRecyclerView.Adapter<SettingsSharedShortcutsSelectorAdapter.ViewHolder>(
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
                ItemSettingsSharedShortcutsSelectorAppBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            Item.ItemType.SHORTCUT -> ViewHolder.Shortcut(
                ItemSettingsSharedShortcutsSelectorShortcutBinding.inflate(
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
            is ViewHolder.Shortcut -> holder.binding.setup(
                item as Item.Shortcut,
                holder.lifecycle
            )
        }
    }

    private fun ItemSettingsSharedShortcutsSelectorAppBinding.setup(
        item: Item.App,
        lifecycle: Lifecycle
    ) {
        itemSettingsSharedShortcutsSelectorAppLabel.text = item.name
        val uri = Uri.parse("${AppIconRequestHandler.SCHEME_PNAME}:${item.packageName}")
        picasso.load(uri).into(itemSettingsSharedShortcutsSelectorAppIcon)
        itemSettingsSharedShortcutsSelectorAppChevron.rotation = if (item.isOpen) 180f else 0f
        lifecycle.coroutineScope.launchWhenResumed {
            root.onClicked().collect {
                toggleItem(item, itemSettingsSharedShortcutsSelectorAppChevron)
            }
        }
        lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsSharedShortcutsSelectorAppChevron.onClicked().collect {
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

    private fun ItemSettingsSharedShortcutsSelectorShortcutBinding.setup(
        item: Item.Shortcut,
        lifecycle: Lifecycle
    ) {
        root.isVisible = item.isVisible
        root.updateLayoutParams<RecyclerView.LayoutParams> {
            height = if (item.isVisible) RecyclerView.LayoutParams.WRAP_CONTENT else 0
        }
        itemSettingsSharedShortcutsSelectorShortcutLabel.text = item.name
        val component = ComponentName(item.packageName, item.activity)
        val uri = Uri.parse("${ComponentNameIconRequestHandler.SCHEME_PNAME}:${component.flattenToString()}")
        picasso.load(uri).into(itemSettingsSharedShortcutsSelectorShortcutIcon)
        lifecycle.coroutineScope.launchWhenResumed {
            root.onClicked().collect {
                onShortcutClicked(component)
            }
        }
    }

    sealed class ViewHolder(open val binding: ViewBinding) :
        LifecycleAwareRecyclerView.ViewHolder(binding.root) {
        data class App(override val binding: ItemSettingsSharedShortcutsSelectorAppBinding) :
            ViewHolder(binding)

        data class Shortcut(override val binding: ItemSettingsSharedShortcutsSelectorShortcutBinding) :
            ViewHolder(binding)
    }

}