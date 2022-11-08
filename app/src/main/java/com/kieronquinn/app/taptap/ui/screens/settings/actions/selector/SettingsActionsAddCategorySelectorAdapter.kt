package com.kieronquinn.app.taptap.ui.screens.settings.actions.selector

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.ItemSettingsActionsAddCategorySelectorCategoryBinding
import com.kieronquinn.app.taptap.models.action.TapTapActionCategory
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.monetcompat.core.MonetCompat

class SettingsActionsAddCategorySelectorAdapter(
    recyclerView: RecyclerView,
    var items: List<TapTapActionCategory>,
    private val onItemClicked: (TapTapActionCategory) -> Unit
) : LifecycleAwareRecyclerView.Adapter<SettingsActionsAddCategorySelectorAdapter.ViewHolder>(
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
            ItemSettingsActionsAddCategorySelectorCategoryBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.setup(items[position], holder.lifecycle)
    }

    private fun ItemSettingsActionsAddCategorySelectorCategoryBinding.setup(category: TapTapActionCategory, lifecycle: Lifecycle) {
        val context = root.context
        root.applyBackgroundTint(monet)
        itemSettingsActionsAddCategorySelectorCategoryTitle.text =
            context.getString(category.labelRes)
        itemSettingsActionsAddCategorySelectorCategoryContent.text =
            context.getText(category.descRes)
        itemSettingsActionsAddCategorySelectorCategoryIcon.setImageResource(category.icon)
        lifecycle.coroutineScope.launchWhenResumed {
            root.onClicked().collect {
                onItemClicked(category)
            }
        }
    }

    class ViewHolder(val binding: ItemSettingsActionsAddCategorySelectorCategoryBinding) :
        LifecycleAwareRecyclerView.ViewHolder(binding.root)

}