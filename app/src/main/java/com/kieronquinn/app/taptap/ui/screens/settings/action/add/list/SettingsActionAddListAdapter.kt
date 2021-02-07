package com.kieronquinn.app.taptap.ui.screens.settings.action.add.list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.ItemActionPickerBinding
import com.kieronquinn.app.taptap.models.TapAction

class SettingsActionAddListAdapter(context: Context, var items: Array<TapAction>, private val cardClickListener: (TapAction) -> Unit): RecyclerView.Adapter<SettingsActionAddListAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemActionPickerBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding){
            itemActionName.setText(item.nameRes)
            itemActionDescription.setText(item.descriptionRes)
            itemActionIcon.setImageResource(item.iconRes)
            root.setOnClickListener {
                cardClickListener.invoke(item)
            }
        }
    }

    class ViewHolder(val binding: ItemActionPickerBinding): RecyclerView.ViewHolder(binding.root)
}