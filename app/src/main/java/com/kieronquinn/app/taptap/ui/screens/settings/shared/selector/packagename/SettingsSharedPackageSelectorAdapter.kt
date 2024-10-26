package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.ItemSettingsSharedPackageSelectorAppBinding
import com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.packagename.SettingsSharedPackageSelectorViewModel.App
import com.kieronquinn.app.taptap.ui.views.LifecycleAwareRecyclerView
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.app.taptap.utils.picasso.AppIconRequestHandler
import com.squareup.picasso.Picasso

class SettingsSharedPackageSelectorAdapter(
    recyclerView: RecyclerView,
    var items: List<App>,
    private val onAppClicked: (packageName: String) -> Unit
) :
    LifecycleAwareRecyclerView.Adapter<SettingsSharedPackageSelectorAdapter.ViewHolder>(
        recyclerView
    ) {

    init {
        setHasStableIds(true)
    }

    private val layoutInflater by lazy {
        recyclerView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    private val picasso = Picasso.get()

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSettingsSharedPackageSelectorAppBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.setup(item, holder.lifecycle)
    }

    override fun getItemId(position: Int): Long {
        return items[position].packageName.hashCode().toLong()
    }

    private fun ItemSettingsSharedPackageSelectorAppBinding.setup(
        item: App,
        lifecycle: Lifecycle
    ) {
        itemSettingsSharedPackageSelectorAppLabel.text = item.name
        itemSettingsSharedPackageSelectorAppPackage.text = item.packageName
        val uri = Uri.parse("${AppIconRequestHandler.SCHEME_PNAME}:${item.packageName}")
        picasso.load(uri).into(itemSettingsSharedPackageSelectorAppIcon)
        lifecycle.whenResumed {
            root.onClicked().collect {
                onAppClicked(item.packageName)
            }
        }
    }

    data class ViewHolder(val binding: ItemSettingsSharedPackageSelectorAppBinding) : LifecycleAwareRecyclerView.ViewHolder(binding.root)

}