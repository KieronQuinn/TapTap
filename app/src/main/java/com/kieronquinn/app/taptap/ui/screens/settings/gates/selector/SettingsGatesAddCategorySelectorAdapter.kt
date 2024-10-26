package com.kieronquinn.app.taptap.ui.screens.settings.gates.selector

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.ItemSettingsGatesAddCategorySelectorCategoryBinding
import com.kieronquinn.app.taptap.models.gate.TapTapGateCategory
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.monetcompat.core.MonetCompat

class SettingsGatesAddCategorySelectorAdapter(
    recyclerView: RecyclerView,
    var items: List<TapTapGateCategory>,
    private val onItemClicked: (TapTapGateCategory) -> Unit
) : LifecycleAwareRecyclerView.Adapter<SettingsGatesAddCategorySelectorAdapter.ViewHolder>(
    recyclerView
) {

    private val layoutInflater by lazy {
        recyclerView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSettingsGatesAddCategorySelectorCategoryBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.setup(items[position], holder.lifecycle)
    }

    private fun ItemSettingsGatesAddCategorySelectorCategoryBinding.setup(category: TapTapGateCategory, lifecycle: Lifecycle) {
        val context = root.context
        root.applyBackgroundTint(monet)
        itemSettingsGatesAddCategorySelectorCategoryTitle.text =
            context.getString(category.labelRes)
        itemSettingsGatesAddCategorySelectorCategoryContent.text =
            context.getText(category.descRes)
        itemSettingsGatesAddCategorySelectorCategoryIcon.setImageResource(category.icon)
        lifecycle.whenResumed {
            root.onClicked().collect {
                onItemClicked(category)
            }
        }
    }

    class ViewHolder(val binding: ItemSettingsGatesAddCategorySelectorCategoryBinding) :
        LifecycleAwareRecyclerView.ViewHolder(binding.root)

}