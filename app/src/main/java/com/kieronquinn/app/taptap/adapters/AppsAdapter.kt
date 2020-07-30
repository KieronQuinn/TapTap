package com.kieronquinn.app.taptap.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_app.view.*
import android.net.Uri
import android.widget.CheckBox
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.App
import com.kieronquinn.app.taptap.utils.AppIconRequestHandler
import com.squareup.picasso.Picasso


class AppsAdapter(context: Context, var apps : List<App>, private val enabledApps: List<String>, private val onAppSelected : (packageName: String) -> Unit) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            layoutInflater.inflate(R.layout.item_app, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.itemView.title.text = app.appName
        val uri = Uri.parse("${AppIconRequestHandler.SCHEME_PNAME}:${app.packageName}")
        Picasso.get().load(uri).into(holder.itemView.icon)
        holder.itemView.setOnClickListener {
            onAppSelected.invoke(app.packageName)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}