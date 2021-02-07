package com.kieronquinn.app.taptap.ui.screens.picker.app

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.ItemAppBinding
import com.kieronquinn.app.taptap.utils.AppIconRequestHandler
import com.squareup.picasso.Picasso

class AppPickerAdapter(context: Context, var apps: List<AppPickerViewModel.App>, private val appClickCallback: (AppPickerViewModel.App) -> Unit): RecyclerView.Adapter<AppPickerAdapter.ViewHolder>() {

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount() = apps.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAppBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = with(holder.binding) {
        val item = apps[holder.adapterPosition]
        title.text = item.label
        val uri = Uri.parse("${AppIconRequestHandler.SCHEME_PNAME}:${item.packageName}")
        Picasso.get().load(uri).into(icon)
        root.setOnClickListener {
            appClickCallback.invoke(item)
        }
    }

    class ViewHolder(val binding: ItemAppBinding): RecyclerView.ViewHolder(binding.root)
}