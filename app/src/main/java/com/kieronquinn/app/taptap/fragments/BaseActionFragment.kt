package com.kieronquinn.app.taptap.fragments

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.adapters.ActionAdapter
import com.kieronquinn.app.taptap.fragments.bottomsheets.ActionBottomSheetFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.GateBottomSheetFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.MaterialBottomSheetDialogFragment
import com.kieronquinn.app.taptap.fragments.gate.GateListFragment
import com.kieronquinn.app.taptap.models.*
import com.kieronquinn.app.taptap.utils.*
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_actions.*
import kotlinx.android.synthetic.main.item_action.view.*

abstract class BaseActionFragment : BaseFragment() {

    companion object {
        const val addResultKey = "ADD_ACTION_RESULT"
        const val addResultKeyGate = "ADD_ACTION_GATE_RESULT"
        const val PREF_KEY_ACTION_HELP_SHOWN = "action_help_shown"
    }

    internal val recyclerView by lazy {
        recycler_view
    }

    internal val fab by lazy {
        fab_action
    }

    private val animationAddToDelete by lazy {
        context?.let {
            if (!isAdded) null
            else ContextCompat.getDrawable(it, R.drawable.ic_add_to_delete) as AnimatedVectorDrawable
        }
    }

    private val animationDeleteToAdd by lazy {
        context?.let {
            if (!isAdded) null
            else ContextCompat.getDrawable(it, R.drawable.ic_delete_to_add) as AnimatedVectorDrawable
        }
    }

    abstract val actions: MutableList<ActionInternal>

    private val itemTouchHelper by lazy {

        var isFabDrop = false

        var draggingItem: ActionInternal? = null
        var draggingViewHolder: RecyclerView.ViewHolder? = null

        val simpleItemTouchCallback =
                object : ItemTouchHelper.SimpleCallback(UP or DOWN or START or END, 0) {

                    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                        val adapter = recyclerView.adapter as ActionAdapter
                        val from = viewHolder.adapterPosition
                        val to = target.adapterPosition
                        if(to == 0 || from == 0) return false
                        adapter.moveItem(from, to)
                        adapter.notifyItemMoved(from, to)
                        saveToFile()
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    }

                    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                        super.onSelectedChanged(viewHolder, actionState)
                        if (actionState == ACTION_STATE_DRAG) {
                            viewHolder?.itemView?.run {
                                val position = viewHolder.adapterPositionAdjusted(true)
                                if(position < 0) return
                                draggingItem = actions[position]
                                draggingViewHolder = viewHolder
                                fakeCard.cloneSize(this)
                                viewHolder.itemView.visibility = View.INVISIBLE
                                fakeCard.item_action_when.visibility = viewHolder.itemView.item_action_when.visibility
                                fakeCard.item_action_blocked_info.visibility = viewHolder.itemView.item_action_blocked_info.visibility
                                fakeCard.visibility = View.VISIBLE
                                fakeCard.item_action_chips.adapter = viewHolder.itemView.item_action_chips.adapter
                                fakeCard.item_action_chips.layoutManager?.apply {
                                    this as LinearLayoutManager
                                    scrollToPositionWithOffset(0, -1 * viewHolder.itemView.item_action_chips.computeHorizontalScrollOffset())
                                }
                                fakeCard.item_action_name.text = item_action_name.text
                                fakeCard.item_action_icon.setImageDrawable(item_action_icon.drawable)
                                fakeCard.item_action_description.text = item_action_description.text
                            }
                            setFabState(true)
                        }
                        if (actionState == ACTION_STATE_IDLE && fakeCard.isOverlapping(fab)) {
                            val action = draggingItem ?: return
                            val draggedViewHolder = draggingViewHolder ?: return
                            val position = recyclerView.layoutManager?.getPosition(draggedViewHolder.itemView)
                                    ?: -1
                            actions.remove(action)
                            recyclerView.adapter?.notifyItemRemoved(position)
                            recyclerView.adapter?.run {
                                this as ActionAdapter
                                notifyListener()
                            }
                            //Fix for item hanging around after removal
                            draggedViewHolder.itemView.run {
                                (parent as ViewGroup).removeView(this)
                            }
                            saveToFile()
                        }
                    }

                    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                        super.clearView(recyclerView, viewHolder)
                        viewHolder.itemView.visibility = View.VISIBLE
                        fakeCard.visibility = View.GONE
                        setFabState(false)
                        recyclerView.adapter?.run {
                            this as ActionAdapter
                            notifyItemChanged(viewHolder.adapterPositionAdjusted(true))
                            notifyAllItemsBelowBarrier()
                        }
                    }

                    override fun onChildDrawOver(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                        viewHolder?.itemView?.run {
                            fakeCard.clonePosition(this)
                            if (fakeCard.isOverlapping(fab)) {
                                if (!isFabDrop) {
                                    fakeCard.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(fab.context, R.color.fab_color_delete))
                                    fab.text = getString(R.string.fab_remove_action_drop)
                                    TransitionManager.beginDelayedTransition(fab.parent as ViewGroup)
                                    isFabDrop = true
                                }
                            } else {
                                if (isFabDrop) {
                                    fakeCard.backgroundTintList = null
                                    fab.text = getString(R.string.fab_remove_action)
                                    TransitionManager.beginDelayedTransition(fab.parent as ViewGroup)
                                    isFabDrop = false
                                }
                            }
                        }
                        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    }

                    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                        if (viewHolder is ActionAdapter.HeaderViewHolder) return 0
                        return super.getMovementFlags(recyclerView, viewHolder)
                    }
                }
        ItemTouchHelper(simpleItemTouchCallback)
    }

    private fun notifyAllItemsBelowBarrier(){
        recyclerView?.adapter?.run {
            this as ActionAdapter
            actions.find { it.isBlocking() }?.let {
                val index = actions.indexOf(it)
                for(i in index until itemCount){
                    notifyItemChanged(i + 1)
                }
            }
        }
    }

    private fun setFabState(removeEnabled: Boolean) {
        if (!isAdded) return
        val colorRemove = ContextCompat.getColor(fab.context, R.color.fab_color_delete)
        val colorAdd = ContextCompat.getColor(fab.context, R.color.fab_color)
        if (removeEnabled) {
            fab.text = fab.context.getString(R.string.fab_remove_action)
            fab.icon = animationAddToDelete
            animationAddToDelete?.start()
            fab.animateBackgroundStateChange(colorAdd, colorRemove)
            fab.isClickable = false
            fab.isFocusable = false
        } else {
            fab.text = fab.context.getString(R.string.fab_add_action)
            fab.icon = animationDeleteToAdd
            animationDeleteToAdd?.start()
            fab.animateBackgroundStateChange(colorRemove, colorAdd)
            fab.isClickable = true
            fab.isFocusable = true
        }
        TransitionManager.beginDelayedTransition(fab.parent as ViewGroup)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = ActionAdapter(recyclerView.context, actions, isTripleTap = this is SettingsActionTripleFragment) {
            itemTouchHelper.startDrag(it)
        }.apply {
            chipAddCallback = { position, gates ->
                showGateBottomSheet(position, gates)
            }
            chipClickCallback = {
                Toast.makeText(context, getString(it.whenDescriptionRes), Toast.LENGTH_LONG).show()
            }
            saveCallback = {
                saveToFile()
                notifyAllItemsBelowBarrier()
            }
            headerCallback = {
                showHelpBottomSheet()
            }
            listChangeListener = {
                if(it > 1){
                    if(recyclerView.visibility != View.VISIBLE){
                        empty_state.fadeOut{}
                        recyclerView.fadeIn{}
                    }
                }else{
                    if(recyclerView.visibility == View.VISIBLE){
                        recyclerView.fadeOut {}
                        empty_state.fadeIn{}
                    }
                }
            }
        }
        fab.applySystemWindowInsetsToMargin(bottom = true)
        fab.post {
            setupRecyclerView(recyclerView, extraTopPadding = if(this is SettingsActionTripleFragment) context?.getToolbarHeight() ?: 0 else 0, extraBottomPadding = fab.height + fab.marginBottom)
        }
        fab.setOnClickListener {
            showActionBottomSheet()
        }
        itemTouchHelper.attachToRecyclerView(recyclerView)
        setFragmentResultListener(addResultKey) { key, bundle ->
            val newItem = bundle.get(addResultKey) as ActionInternal
            actions.add(newItem)
            recyclerView.adapter?.notifyItemInserted(actions.size)
            recyclerView.adapter?.run {
                this as ActionAdapter
                notifyItemChanged(currentInfoPosition ?: 0)
                notifyListener()
            }
            recyclerView?.layoutManager?.scrollToPosition(actions.size)
            saveToFile()
        }
        if (sharedPreferences?.getBoolean(PREF_KEY_ACTION_HELP_SHOWN, false) == false) {
            showHelpBottomSheet()
        }
        //Disable fake card's action list
        fakeCard.item_action_chips.setOnTouchListener { v, event -> true }
    }

    private fun showActionBottomSheet() {
        ActionBottomSheetFragment().show(parentFragmentManager, "bs_action")
    }

    private fun showGateBottomSheet(position: Int, currentGates: Array<TapGate>) {
        setFragmentResultListener(addResultKeyGate){ key, bundle ->
            val newItem = bundle.get(addResultKeyGate) as GateInternal
            val convertedItem = WhenGateInternal(newItem.gate, false, newItem.data)
            recyclerView?.adapter?.run {
                this as ActionAdapter
                actions[position].whenList.add(convertedItem)
                recyclerView?.findViewHolderForAdapterPosition(position + 1)?.itemView?.item_action_chips?.adapter?.run {
                    notifyItemInserted(itemCount - 1)
                }
                notifyItemChanged(position + 1)
                notifyAllItemsBelowBarrier()
                saveToFile()
            }
            Log.d("ConvertedItem", "Item ${convertedItem.gate.name} ${convertedItem.data}")
        }
        GateBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putInt(GateListFragment.KEY_POSITION, position)
                putSerializable(GateListFragment.KEY_PASSED_GATES, TapGate.values().filter { it.dataType != null || !currentGates.contains(it) }.toTypedArray())
            }
        }.show(parentFragmentManager, "bs_when_gates")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeAsUpEnabled(true)
    }

    abstract fun saveToFile()

    private fun showHelpBottomSheet() {
        MaterialBottomSheetDialogFragment.create(MaterialBottomSheetDialogFragment(), childFragmentManager, "bs_help"){
            it.title(R.string.bs_help_action_title)
            it.message(R.string.bs_help_action)
        }
        sharedPreferences?.edit()?.putBoolean(PREF_KEY_ACTION_HELP_SHOWN, true)?.apply()
    }

}