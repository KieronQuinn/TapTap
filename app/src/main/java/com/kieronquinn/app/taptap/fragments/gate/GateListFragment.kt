package com.kieronquinn.app.taptap.fragments.gate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.adapters.GateAdapter
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.models.TapGate
import com.kieronquinn.app.taptap.models.store.GateListFile
import com.kieronquinn.app.taptap.utils.CONFIGURABLE_GATES
import com.kieronquinn.app.taptap.utils.dip

class GateListFragment : Fragment(), GateAdapter.GateCallback {

    private var toolbarListener: ((Boolean) -> Unit)? = null

    private var itemClickListener: ((GateInternal) -> Unit)? = null

    companion object {
        const val KEY_PASSED_GATES = "passed_gates"
        const val KEY_POSITION = "position"
    }

    private val passedGates by lazy {
        if(arguments?.containsKey(KEY_PASSED_GATES) == true) {
            arguments?.getSerializable(KEY_PASSED_GATES) as Array<TapGate>
        }else null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_action_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        val currentGates = GateListFile.loadFromFile(recyclerView.context).map { it.gate }
        val gates = TapGate.values().mapNotNull {
            if(passedGates != null){
                if(passedGates?.contains(it) == true){
                    GateInternal(it, false)
                }else{
                    null
                }
            }else{
                if(!CONFIGURABLE_GATES.contains(it) && currentGates.contains(it)) null
                else GateInternal(it, false)
            }
        }.toMutableList()
        val adapter = GateAdapter(recyclerView.context, gates, true, this, passedGates != null)
        recyclerView.adapter = adapter
        recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
            toolbarListener?.invoke(recyclerView.computeVerticalScrollOffset() > 0)
        }
        recyclerView.setOnApplyWindowInsetsListener { v, insets ->
            v.setPadding(v.paddingLeft, v.paddingTop, 0, insets.systemWindowInsetBottom + v.context.dip(8))
            insets
        }
    }

    fun getToolbarTitle(): String {
        return if(passedGates != null) getString(R.string.add_gate_when) else getString(R.string.fab_add_gate)
    }

    fun setToolbarListener(listener: (Boolean) -> Unit){
        this.toolbarListener = listener
    }

    fun setItemClickListener(listener: (GateInternal) -> Unit){
        this.itemClickListener = listener
    }

    override fun onGateChange(gates: List<GateInternal>) {
        val selectedGate = gates.first()
        itemClickListener?.invoke(selectedGate)
    }

    override fun onGateSelected() {
    }

    override fun onGateDeselected() {
    }

}