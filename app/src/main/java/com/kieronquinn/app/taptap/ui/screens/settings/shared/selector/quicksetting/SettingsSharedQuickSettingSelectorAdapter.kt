package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.quicksetting

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsSharedQuickSettingsHeaderBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsSharedQuickSettingsItemBinding
import com.kieronquinn.app.taptap.repositories.quicksettings.QuickSettingsRepository.QuickSetting
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.picasso.ComponentNameIconRequestHandler
import com.kieronquinn.monetcompat.core.MonetCompat
import com.squareup.picasso.Picasso

class SettingsSharedQuickSettingSelectorAdapter(
    recyclerView: LifecycleAwareRecyclerView,
    var items: List<QuickSetting>,
    private val onQuickSettingClicked: (QuickSetting) -> Unit
) : LifecycleAwareRecyclerView.Adapter<SettingsSharedQuickSettingSelectorAdapter.ViewHolder>(
    recyclerView
) {

    private val layoutInflater by lazy {
        recyclerView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    private val picasso by lazy {
        Picasso.get()
    }

    override fun getItemCount() = items.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            0 -> ViewHolder.Header(
                ItemSettingsSharedQuickSettingsHeaderBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            1 -> ViewHolder.Item(
                ItemSettingsSharedQuickSettingsItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            else -> throw RuntimeException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.Header -> holder.binding.setupHeader()
            is ViewHolder.Item -> holder.binding.setupItem(holder.lifecycle, items[position - 1])
        }
    }

    private fun ItemSettingsSharedQuickSettingsHeaderBinding.setupHeader() {
        root.applyBackgroundTint(monet)
    }

    private fun ItemSettingsSharedQuickSettingsItemBinding.setupItem(lifecycle: Lifecycle, item: QuickSetting) {
        val uri = Uri.parse("${ComponentNameIconRequestHandler.SCHEME_PNAME}:${item.component.flattenToString()}")
        picasso.load(uri).into(itemSettingsSharedQuickSettingsAppIcon)
        itemSettingsSharedQuickSettingsAppLabel.text = item.label ?: item.packageLabel
        itemSettingsSharedQuickSettingsAppComponent.text = item.component.getShortName()
        lifecycle.coroutineScope.launchWhenResumed {
            root.onClicked().collect {
                onQuickSettingClicked(item)
            }
        }
    }

    private fun ComponentName.getShortName(): String {
        return shortClassName.run {
            if(contains(".")){
                substring(lastIndexOf("."))
            }else this
        }
    }

    sealed class ViewHolder(open val binding: ViewBinding) :
        LifecycleAwareRecyclerView.ViewHolder(binding.root) {
        data class Header(override val binding: ItemSettingsSharedQuickSettingsHeaderBinding) :
            ViewHolder(binding)
        data class Item(override val binding: ItemSettingsSharedQuickSettingsItemBinding) :
            ViewHolder(binding)
    }
}