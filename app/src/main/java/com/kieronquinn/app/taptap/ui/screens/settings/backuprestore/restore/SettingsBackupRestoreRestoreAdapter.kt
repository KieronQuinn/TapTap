package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ItemSettingsBackupRestoreRestoreHeaderBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsBackupRestoreRestoreInfoboxBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsBackupRestoreRestoreRequirementBinding
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel.Item
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.core.MonetCompat

class SettingsBackupRestoreRestoreAdapter(
    recyclerView: LifecycleAwareRecyclerView,
    var items: List<Item>,
    private val onSetupClicked: (Item.Requirement) -> Unit,
    private val onSkipClicked: (String) -> Unit
) :
    LifecycleAwareRecyclerView.Adapter<SettingsBackupRestoreRestoreAdapter.ViewHolder>(recyclerView) {

    init {
        setHasStableIds(true)
    }

    private val layoutInflater by lazy {
        recyclerView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val googleSansTextMedium by lazy {
        ResourcesCompat.getFont(recyclerView.context, R.font.google_sans_text_medium)
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

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long {
        return when (val item = items[position]) {
            is Item.Infobox -> item.content.toLong()
            is Item.Header -> item.title.toLong()
            is Item.Requirement -> item.uuid.hashCode().toLong()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (Item.Type.values()[viewType]) {
            Item.Type.INFOBOX -> ViewHolder.Infobox(
                ItemSettingsBackupRestoreRestoreInfoboxBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            Item.Type.HEADER -> ViewHolder.Header(
                ItemSettingsBackupRestoreRestoreHeaderBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
            )
            Item.Type.REQUIREMENT -> ViewHolder.Requirement(
                ItemSettingsBackupRestoreRestoreRequirementBinding.inflate(
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
            is ViewHolder.Infobox -> holder.binding.setup(item as Item.Infobox)
            is ViewHolder.Header -> holder.binding.setup(item as Item.Header)
            is ViewHolder.Requirement -> holder.binding.setup(item as Item.Requirement, holder.lifecycle)
        }
    }

    private fun ItemSettingsBackupRestoreRestoreInfoboxBinding.setup(item: Item.Infobox) {
        root.applyBackgroundTint(monet)
        itemSettingsGatesInfoContent.text = root.context.getText(item.content)
    }

    private fun ItemSettingsBackupRestoreRestoreHeaderBinding.setup(item: Item.Header) {
        itemSettingsBackupRestoreRestoreHeaderTitle.text = root.context.getText(item.title)
    }

    private fun ItemSettingsBackupRestoreRestoreRequirementBinding.setup(item: Item.Requirement, lifecycle: Lifecycle) {
        root.alpha = if(item.isSupported) 1f else 0.5f
        root.applyBackgroundTint(monet)
        itemSettingsBackupRestoreRestoreRequirementSetup.isVisible = item.isSupported
        itemSettingsBackupRestoreRestoreRequirementSkip.isVisible = item.isSupported
        itemSettingsBackupRestoreRestoreRequirementIcon.setImageResource(item.icon)
        itemSettingsBackupRestoreRestoreRequirementTitle.text = root.context.getText(item.title)
        itemSettingsBackupRestoreRestoreRequirementContent.text = if(item.isSupported){
            root.context.getText(item.desc)
        }else{
            if(item.isGate){
                root.context.getText(R.string.gate_selector_unavailable)
            }else{
                root.context.getText(R.string.action_selector_unavailable)
            }
        }
        itemSettingsBackupRestoreRestoreRequirementChip.run {
            text = root.context.getString(item.chipText)
            chipIcon = ContextCompat.getDrawable(root.context, item.chipIcon)
            typeface = googleSansTextMedium
            chipBackgroundColor = chipBackground
        }
        lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsBackupRestoreRestoreRequirementSetup.onClicked().collect {
                onSetupClicked(item)
            }
        }
        lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsBackupRestoreRestoreRequirementSkip.onClicked().collect {
                onSkipClicked(item.uuid)
            }
        }
    }

    sealed class ViewHolder(open val binding: ViewBinding) :
        LifecycleAwareRecyclerView.ViewHolder(binding.root) {
        data class Infobox(override val binding: ItemSettingsBackupRestoreRestoreInfoboxBinding) :
            ViewHolder(binding)

        data class Header(override val binding: ItemSettingsBackupRestoreRestoreHeaderBinding) :
            ViewHolder(binding)

        data class Requirement(override val binding: ItemSettingsBackupRestoreRestoreRequirementBinding) :
            ViewHolder(binding)
    }

}