package com.kieronquinn.app.taptap.ui.screens.settings.gate.add.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.ItemGateBinding
import com.kieronquinn.app.taptap.models.TapGate

class SettingsGateAddListAdapter(context: Context, private val isWhenGateFlow: Boolean, private val gates: Array<TapGate>, private val itemClickCallback: (TapGate) -> Unit): RecyclerView.Adapter<SettingsGateAddListAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount() = gates.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGateBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding) {
        val item = gates[holder.adapterPosition]
        itemGateIcon.setImageResource(item.iconRes)
        itemGateName.setText(item.nameRes)
        itemGateDescription.setText(if(isWhenGateFlow) item.whenDescriptionRes else item.descriptionRes)
        itemGateSwitch.visibility = View.INVISIBLE
        itemGateAdd.isVisible = true
        root.setOnClickListener {
            itemClickCallback.invoke(item)
        }
    }

    data class ViewHolder(val binding: ItemGateBinding): RecyclerView.ViewHolder(binding.root)
}