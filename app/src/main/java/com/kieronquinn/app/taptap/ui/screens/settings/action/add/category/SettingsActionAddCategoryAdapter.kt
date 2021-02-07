package com.kieronquinn.app.taptap.ui.screens.settings.action.add.category

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.ItemAddCategoryBinding
import com.kieronquinn.app.taptap.models.TapActionCategory

class SettingsActionAddCategoryAdapter(context: Context, private val categoryClickListener: (TapActionCategory) -> Unit): RecyclerView.Adapter<SettingsActionAddCategoryAdapter.ViewHolder>() {

    private val categories = TapActionCategory.values()

    private val layoutInference by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount(): Int = categories.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAddCategoryBinding.inflate(layoutInference, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categories[position]
        with(holder.binding.addActionCategoryTitle){
            setText(item.labelRes)
            setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(context, item.icon), null, null, null)
        }
        holder.binding.addActionCategoryCard.setOnClickListener {
            categoryClickListener.invoke(item)
        }
    }

    class ViewHolder(val binding: ItemAddCategoryBinding): RecyclerView.ViewHolder(binding.root)

}