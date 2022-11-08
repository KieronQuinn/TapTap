package com.kieronquinn.app.taptap.ui.screens.settings.modelpicker

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.databinding.ItemSettingsModelPickerHeaderBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsModelPickerModelBinding
import com.kieronquinn.app.taptap.ui.screens.settings.modelpicker.SettingsModelPickerViewModel.Item
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.views.applyMonet

class SettingsModelPickerAdapter(recyclerView: LifecycleAwareRecyclerView, var items: List<Item>, val onItemSelected: (TapModel) -> Unit): LifecycleAwareRecyclerView.Adapter<SettingsModelPickerAdapter.ViewHolder>(recyclerView) {

    init {
        setHasStableIds(true)
    }

    private val context = recyclerView.context

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].itemType.ordinal
    }

    override fun getItemId(position: Int): Long {
        val item = items[position]
        return when(item){
            is Item.Header -> -1L
            is Item.Model -> item.model.ordinal.toLong()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(Item.ItemType.values()[viewType]){
            Item.ItemType.HEADER -> ViewHolder.Header(ItemSettingsModelPickerHeaderBinding.inflate(layoutInflater, parent, false))
            Item.ItemType.MODEL -> ViewHolder.Model(ItemSettingsModelPickerModelBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder){
            is ViewHolder.Model -> holder.binding.setup(items[position] as Item.Model, holder.lifecycle)
            is ViewHolder.Header -> holder.binding.setup(items[position] as Item.Header)
        }
    }

    private fun ItemSettingsModelPickerModelBinding.setup(item: Item.Model, lifecycle: Lifecycle) {
        itemSettingsModelPickerTitle.text = context.getString(item.model.nameRes)
        itemSettingsModelPickerContent.text = if(item.best) {
            context.getString(R.string.settings_model_picker_recommended, context.getString(item.model.descriptionRes))
        }else{
            context.getString(item.model.descriptionRes)
        }
        itemSettingsModelPickerRadio.isChecked = item.selected
        itemSettingsModelPickerRadio.applyMonet()
        root.applyBackgroundTint(monet)
        lifecycle.coroutineScope.launchWhenResumed {
            root.onClicked().collect {
                onItemSelected(item.model)
            }
        }
        lifecycle.coroutineScope.launchWhenResumed {
            itemSettingsModelPickerRadio.onClicked().collect {
                onItemSelected(item.model)
            }
        }
    }

    private fun ItemSettingsModelPickerHeaderBinding.setup(item: Item.Header) {
        root.applyBackgroundTint(monet)
        itemSettingsModelPickerHeaderContent.text = root.context.getText(item.contentRes)
    }

    sealed class ViewHolder(open val binding: ViewBinding): LifecycleAwareRecyclerView.ViewHolder(binding.root) {
        data class Model(override val binding: ItemSettingsModelPickerModelBinding): ViewHolder(binding)
        data class Header(override val binding: ItemSettingsModelPickerHeaderBinding): ViewHolder(binding)
    }

}