package com.kieronquinn.app.taptap.fragments

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.adapters.GateAdapter
import com.kieronquinn.app.taptap.fragments.bottomsheets.GateBottomSheetFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.GenericBottomSheetFragment
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.models.store.GateListFile
import com.kieronquinn.app.taptap.utils.animateBackgroundStateChange
import com.kieronquinn.app.taptap.utils.sharedPreferences
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_gates.*

class SettingsGateFragment : BaseFragment(), GateAdapter.GateCallback {

    companion object{
        const val addResultKey = "ADD_GATE_RESULT"
        const val PREF_KEY_GATE_HELP_SHOWN = "gate_help_shown"
    }

    private val recyclerView by lazy {
        recycler_view
    }

    private val fab by lazy {
        fab_gate
    }

    private val gates by lazy {
        GateListFile.loadFromFile(requireContext()).toMutableList()
    }

    private val animationAddToDelete by lazy {
        context?.let {
            if(!isAdded) null
            else ContextCompat.getDrawable(it, R.drawable.ic_add_to_delete) as AnimatedVectorDrawable
        }
    }

    private val animationDeleteToAdd by lazy {
        context?.let {
            if(!isAdded) null
            else ContextCompat.getDrawable(it, R.drawable.ic_delete_to_add) as AnimatedVectorDrawable
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = GateAdapter(recyclerView.context, gates, false, this)
        recyclerView.adapter = adapter
        fab.applySystemWindowInsetsToMargin(bottom = true)
        fab.post {
            setupRecyclerView(recyclerView, extraBottomPadding = fab.height + fab.marginBottom, offsetForSystem = true)
        }
        fab.setOnClickListener {
            if(adapter.isItemSelected){
                adapter.onDeleteClicked()
            }else{
                showGateBottomSheet()
            }
        }
        setFragmentResultListener(addResultKey){ key, bundle ->
            val newItem = bundle.get(addResultKey) as GateInternal
            gates.add(newItem)
            recyclerView.adapter?.notifyItemInserted(gates.size)
            recyclerView?.layoutManager?.scrollToPosition(gates.size - 1)
            saveToFile()
        }
        if(sharedPreferences?.getBoolean(PREF_KEY_GATE_HELP_SHOWN, false) == false){
            showHelpBottomSheet()
        }
    }

    private fun showGateBottomSheet() {
        GateBottomSheetFragment().show(parentFragmentManager, "bs_gates")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeAsUpEnabled(true)
    }

    private fun setFabState(removeEnabled: Boolean){
        if(!isAdded) return
        val colorRemove = ContextCompat.getColor(fab.context, R.color.fab_color_delete)
        val colorAdd = ContextCompat.getColor(fab.context, R.color.fab_color)
        if(removeEnabled){
            fab.text = fab.context.getString(R.string.fab_remove_action)
            fab.icon = animationAddToDelete
            animationAddToDelete?.start()
            fab.animateBackgroundStateChange(colorAdd, colorRemove)
        }else{
            fab.text = fab.context.getString(R.string.fab_add_gate)
            fab.icon = animationDeleteToAdd
            animationDeleteToAdd?.start()
            fab.animateBackgroundStateChange(colorRemove, colorAdd)
        }
        TransitionManager.beginDelayedTransition(fab.parent as ViewGroup)
    }

    override fun onGateChange(gates: List<GateInternal>) {
        saveToFile()
    }

    override fun onGateSelected() {
        setFabState(true)
    }

    override fun onGateDeselected() {
        setFabState(false)
    }

    override fun onBackPressed() : Boolean {
        val adapter = recyclerView.adapter as GateAdapter
        if(adapter.isItemSelected){
            adapter.deselectItem()
            return true
        }
        return false
    }

    private fun saveToFile(){
        GateListFile.saveToFile(recyclerView.context, gates.toTypedArray(), sharedPreferences)
    }

    private fun showHelpBottomSheet(){
        GenericBottomSheetFragment.create(getString(R.string.bs_help_gate), R.string.bs_help_gate_title, android.R.string.ok).show(childFragmentManager, "bs_help")
        sharedPreferences?.edit()?.putBoolean(PREF_KEY_GATE_HELP_SHOWN, true)?.apply()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_help, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_help -> {
                showHelpBottomSheet()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}