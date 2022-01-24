package com.kieronquinn.app.taptap.ui.screens.settings.actions.selector.actions

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ItemSettingsActionsActionSelectorItemBinding
import com.kieronquinn.app.taptap.models.action.ActionRequirement
import com.kieronquinn.app.taptap.models.action.ActionSupportedRequirement
import com.kieronquinn.app.taptap.models.action.TapTapActionDirectory
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.addRippleForeground
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.removeRippleForeground
import com.kieronquinn.monetcompat.core.MonetCompat

class SettingsActionsActionSelectorAdapter(
    recyclerView: RecyclerView,
    var items: List<TapTapActionDirectory>,
    private val getActionSupportedRequirement: (Context, TapTapActionDirectory) -> ActionSupportedRequirement?,
    private val onChipClicked: (ActionRequirement.UserDisplayedActionRequirement) -> Unit,
    private val onActionClicked: (TapTapActionDirectory) -> Unit
) : LifecycleAwareRecyclerView.Adapter<SettingsActionsActionSelectorAdapter.ViewHolder>(recyclerView) {

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
        ColorStateList.valueOf(monet.getSecondaryColor(recyclerView.context))
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSettingsActionsActionSelectorItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.setup(items[position], holder.lifecycle)
    }

    private fun ItemSettingsActionsActionSelectorItemBinding.setup(action: TapTapActionDirectory, lifecycle: Lifecycle) {
        val context = root.context
        root.backgroundTintList = ColorStateList.valueOf(monet.getPrimaryColor(context))
        val actionSupportedRequirement = getActionSupportedRequirement(context, action)
        if(actionSupportedRequirement == null){
            itemSettingsActionSelectorTitle.alpha = 1f
            itemSettingsActionSelectorContent.alpha = 1f
            itemSettingsActionSelectorIcon.alpha = 1f
            root.background.alpha = 255
            root.addRippleForeground()
            root.setOnClickListener {
                onActionClicked(action)
            }
        }else{
            itemSettingsActionSelectorTitle.alpha = 0.5f
            itemSettingsActionSelectorContent.alpha = 0.5f
            itemSettingsActionSelectorIcon.alpha = 0.5f
            root.background.alpha = 128
            root.removeRippleForeground()
            root.setOnClickListener(null)
        }
        itemSettingsActionSelectorTitle.text = context.getText(action.nameRes)
        itemSettingsActionSelectorIcon.setImageResource(action.iconRes)
        itemSettingsActionSelectorContent.text = if(actionSupportedRequirement == null) {
            context.getText(action.descriptionRes)
        }else{
            context.getString(
                R.string.action_selector_unavailable,
                context.getString(actionSupportedRequirement.description)
            )
        }
        val requirement = if(actionSupportedRequirement == null) action.actionRequirement?.firstOrNull { it is ActionRequirement.UserDisplayedActionRequirement } else null
        itemSettingsActionSelectorChip.isVisible = requirement != null
        if(requirement != null){
            itemSettingsActionSelectorChip.run {
                requirement as ActionRequirement.UserDisplayedActionRequirement
                text = context.getString(requirement.label)
                typeface = googleSansTextMedium
                chipBackgroundColor = chipBackground
                chipIcon = ContextCompat.getDrawable(context, requirement.icon)
            }
        }
        lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsActionSelectorChip.onClicked().collect {
                action.actionRequirement?.firstOrNull { it is ActionRequirement.UserDisplayedActionRequirement }?.let { requirement ->
                    requirement as ActionRequirement.UserDisplayedActionRequirement
                    onChipClicked(requirement)
                }
            }
        }
    }

    class ViewHolder(val binding: ItemSettingsActionsActionSelectorItemBinding): LifecycleAwareRecyclerView.ViewHolder(binding.root)
}