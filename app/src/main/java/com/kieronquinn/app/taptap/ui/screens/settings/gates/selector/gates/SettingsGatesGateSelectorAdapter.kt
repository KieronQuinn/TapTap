package com.kieronquinn.app.taptap.ui.screens.settings.gates.selector.gates

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
import com.kieronquinn.app.taptap.databinding.ItemSettingsGatesGateSelectorItemBinding
import com.kieronquinn.app.taptap.models.gate.GateRequirement
import com.kieronquinn.app.taptap.models.gate.GateSupportedRequirement
import com.kieronquinn.app.taptap.models.gate.TapTapGateDirectory
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.addRippleForeground
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.removeRippleForeground
import com.kieronquinn.monetcompat.core.MonetCompat

class SettingsGatesGateSelectorAdapter(
    recyclerView: RecyclerView,
    var items: List<TapTapGateDirectory>,
    private val getGateSupportedRequirement: (Context, TapTapGateDirectory) -> GateSupportedRequirement?,
    private val onChipClicked: (GateRequirement.UserDisplayedGateRequirement) -> Unit,
    private val onGateClicked: (TapTapGateDirectory) -> Unit,
    private val isRequirement: Boolean
) : LifecycleAwareRecyclerView.Adapter<SettingsGatesGateSelectorAdapter.ViewHolder>(recyclerView) {

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
        return ViewHolder(ItemSettingsGatesGateSelectorItemBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.setup(items[position], holder.lifecycle)
    }

    private fun ItemSettingsGatesGateSelectorItemBinding.setup(gate: TapTapGateDirectory, lifecycle: Lifecycle) {
        val context = root.context
        root.backgroundTintList = ColorStateList.valueOf(monet.getPrimaryColor(context))
        val supportedRequirement = getGateSupportedRequirement(context, gate)
        if(supportedRequirement == null){
            itemSettingsGateSelectorTitle.alpha = 1f
            itemSettingsGateSelectorContent.alpha = 1f
            itemSettingsGateSelectorIcon.alpha = 1f
            root.background.alpha = 255
            root.addRippleForeground()
            root.setOnClickListener {
                onGateClicked(gate)
            }
        }else{
            itemSettingsGateSelectorTitle.alpha = 0.5f
            itemSettingsGateSelectorContent.alpha = 0.5f
            itemSettingsGateSelectorIcon.alpha = 0.5f
            root.background.alpha = 128
            root.removeRippleForeground()
            root.setOnClickListener(null)
        }
        itemSettingsGateSelectorTitle.text = context.getText(gate.nameRes)
        itemSettingsGateSelectorIcon.setImageResource(gate.iconRes)
        itemSettingsGateSelectorContent.text = if(supportedRequirement == null) {
            if(isRequirement) {
                context.getText(gate.whenDescriptionRes)
            }else{
                context.getText(gate.descriptionRes)
            }
        }else{
            context.getString(R.string.gate_selector_unavailable, context.getString(supportedRequirement.description))
        }
        val requirement = if(supportedRequirement == null) gate.gateRequirement?.firstOrNull { it is GateRequirement.UserDisplayedGateRequirement } else null
        itemSettingsGateSelectorChip.isVisible = requirement != null
        if(requirement != null){
            itemSettingsGateSelectorChip.run {
                requirement as GateRequirement.UserDisplayedGateRequirement
                text = context.getString(requirement.label)
                typeface = googleSansTextMedium
                chipBackgroundColor = chipBackground
                chipIcon = ContextCompat.getDrawable(context, requirement.icon)
            }
        }
        lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsGateSelectorChip.onClicked().collect {
                gate.gateRequirement?.firstOrNull { it is GateRequirement.UserDisplayedGateRequirement }?.let { requirement ->
                    requirement as GateRequirement.UserDisplayedGateRequirement
                    onChipClicked(requirement)
                }
            }
        }
    }

    class ViewHolder(val binding: ItemSettingsGatesGateSelectorItemBinding): LifecycleAwareRecyclerView.ViewHolder(binding.root)
}