package com.kieronquinn.app.taptap.ui.screens.settings.action

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ItemActionBinding
import com.kieronquinn.app.taptap.databinding.ItemActionHeaderBinding
import com.kieronquinn.app.taptap.databinding.ItemActionHeaderTripleBinding
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.components.dragrecycler.RecyclerViewItemMoveCallback
import java.util.*

class SettingsActionAdapter(lifecycleOwner: LifecycleOwner, private val viewModel: SettingsActionViewModel, private val context: Context, var items: MutableList<ActionInternal>, private val isTriple: Boolean, private val headerClickListener: () -> Unit): RecyclerView.Adapter<SettingsActionAdapter.ViewHolder>(), RecyclerViewItemMoveCallback.RecyclerViewItemMoveListener {

    init {
        viewModel.selectedState.observe(lifecycleOwner){
            if(it.previousSelectedIndex != -1){
                notifyItemChanged(it.previousSelectedIndex)
            }
            if(it is SettingsActionViewModel.SelectedState.ActionSelected){
                notifyItemChanged(it.selectedItemIndex)
            }
        }
        viewModel.tripleTapEnabled.observe(lifecycleOwner){
            if(isTriple){
                notifyItemChanged(0)
            }
        }
        viewModel.blockingWarning.observe(lifecycleOwner){
            if(it.position != -1){
                notifyItemChanged(it.position)
            }
            if(it.previousPosition != -1){
                notifyItemChanged(it.previousPosition)
            }
        }
        viewModel.state.observe(lifecycleOwner){
            viewModel.updateInfoPosition()
        }
    }

    var chipAddClickListener: ((Int) -> Unit)? = null

    private val layoutInflater by lazy {
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount(): Int = items.size + 1

    override fun getItemViewType(position: Int): Int {
        return when {
            position > 0 -> return ItemType.ITEM.ordinal
            isTriple -> ItemType.HEADER_TRIPLE.ordinal
            else -> ItemType.HEADER_DOUBLE.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(ItemType.values()[viewType]){
            ItemType.HEADER_DOUBLE -> ViewHolder.SettingsActionHeaderDouble(ItemActionHeaderBinding.inflate(layoutInflater, parent, false))
            ItemType.HEADER_TRIPLE -> ViewHolder.SettingsActionHeaderTriple(ItemActionHeaderTripleBinding.inflate(layoutInflater, parent, false))
            ItemType.ITEM -> ViewHolder.SettingsActionItem(ItemActionBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = when(holder) {
        is ViewHolder.SettingsActionHeaderDouble -> {
            holder.itemActionHeaderBinding.root.setOnClickListener {
                headerClickListener.invoke()
            }
        }
        is ViewHolder.SettingsActionHeaderTriple -> {
            holder.itemActionHeaderBinding.run {
                root.isClickable = false
                root.isFocusable = false
                if(viewModel.tripleTapEnabled.value == true){
                    helpActionTripleTitle.isVisible = false
                    root.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.icon_circle_12))
                }else{
                    helpActionTripleTitle.isVisible = true
                    root.setCardBackgroundColor(ContextCompat.getColor(root.context, R.color.accessibility_cross_circle))
                }
            }
        }
        is ViewHolder.SettingsActionItem -> {
            holder.setupItem()
        }
    }

    private fun ViewHolder.SettingsActionItem.setupItem(){
        //Take into account the header
        val adjustedPosition = adapterPosition - 1
        val item = items[adjustedPosition]
        with(itemActionBinding){
            root.run {
                if(adapterPosition == viewModel.getSelectedIndex()){
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_selected))
                }else{
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
                }
            }
            itemActionIcon.setImageResource(item.action.iconRes)
            itemActionDescription.text = item.getCardDescription(root.context) ?: root.context.getText(item.action.descriptionRes)
            itemActionName.text = root.context.getString(item.action.nameRes)
            itemActionWhen.text = item.getCardWhenListHeader(root.context)
            itemActionChips.run {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = SettingsActionChipAdapter(context, viewModel, item.whenList) {
                    chipAddClickListener?.invoke(adjustedPosition)
                }
            }
            itemActionHandle.setOnTouchListener { _, event ->
                if(event.action == MotionEvent.ACTION_DOWN){
                    viewModel.onRecyclerViewDragStart(this@setupItem)
                }
                false
            }
            val blockedPosition = viewModel.blockingWarning.value?.position ?: -1
            itemActionBlockedInfo.root.isVisible = adapterPosition == blockedPosition && adapterPosition != items.size
            root.setOnLongClickListener {
                viewModel.setSelectedIndex(adapterPosition)
                true
            }
        }
    }

    override fun onRowSelected(viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.alpha = 0.5f
    }

    override fun onRowClear(viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.alpha = 1f
        viewModel.commitChanges()
    }

    override fun onRowMoved(from: Int, to: Int) {
        //Take into account the header when swapping in the list...
        val fromPosition = from - 1
        val toPosition = to - 1
        if(fromPosition < 0 || toPosition < 0) return
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        //...but use the adapter from and to when swapping physical locations
        notifyItemMoved(from, to)
        viewModel.swapSelected(from, to)
    }

    sealed class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        data class SettingsActionItem(val itemActionBinding: ItemActionBinding): ViewHolder(itemActionBinding.root)
        data class SettingsActionHeaderDouble(val itemActionHeaderBinding: ItemActionHeaderBinding): ViewHolder(itemActionHeaderBinding.root)
        data class SettingsActionHeaderTriple(val itemActionHeaderBinding: ItemActionHeaderTripleBinding): ViewHolder(itemActionHeaderBinding.root)
    }

    enum class ItemType {
        ITEM, HEADER_DOUBLE, HEADER_TRIPLE
    }
}