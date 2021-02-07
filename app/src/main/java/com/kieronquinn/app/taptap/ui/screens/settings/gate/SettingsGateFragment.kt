package com.kieronquinn.app.taptap.ui.screens.settings.gate

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentGatesBinding
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.utils.extensions.animateBackgroundStateChange
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.components.base.BoundFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsGateFragment: BoundFragment<FragmentGatesBinding>(FragmentGatesBinding::class.java) {

    private val viewModel by viewModel<SettingsGateViewModel>()

    private val animationAddToDelete by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_to_delete) as AnimatedVectorDrawable
    }

    private val animationDeleteToAdd by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_to_add) as AnimatedVectorDrawable
    }

    private val adapter by lazy {
        SettingsGateAdapter(requireContext(), viewModel, viewLifecycleOwner, emptyList<GateInternal>().toMutableList()){
            viewModel.onHeaderClick(this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewModel){
            state.observe(viewLifecycleOwner){
                when(it){
                    is SettingsGateViewModel.State.Loading -> {
                        binding.emptyState.isVisible = false
                    }
                    is SettingsGateViewModel.State.Loaded -> {
                        binding.emptyState.isVisible = false
                        val isAddedItem = adapter.itemCount != 1 && it.gates.size + 1 > adapter.itemCount
                        adapter.items = it.gates
                        adapter.notifyDataSetChanged()
                        if(isAddedItem){
                            binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                        }
                    }
                    is SettingsGateViewModel.State.Empty -> {
                        binding.emptyState.isVisible = true
                    }
                }
            }
            fabState.observe(viewLifecycleOwner){
                binding.setFabState(it)
            }
            getGates()
        }
        with(binding){
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
            fabGate.applySystemWindowInsetsToMargin(bottom = true)
            fabGate.post {
                recyclerView.bindToInsets(fabGate.measuredHeight + fabGate.marginBottom)
            }
            fabGate.setOnClickListener {
                viewModel.onFabClicked(this@SettingsGateFragment, adapter)
            }
        }
    }
    
    private fun FragmentGatesBinding.setFabState(removeEnabled: Boolean){
        val colorRemove = ContextCompat.getColor(fabGate.context, R.color.fab_color_delete)
        val colorAdd = ContextCompat.getColor(fabGate.context, R.color.fab_color)
        if (removeEnabled) {
            fabGate.text = fabGate.context.getString(R.string.fab_remove_action)
            fabGate.icon = animationAddToDelete
            animationAddToDelete.start()
            fabGate.animateBackgroundStateChange(colorAdd, colorRemove)
        } else {
            fabGate.text = fabGate.context.getString(R.string.fab_add_gate)
            fabGate.icon = animationDeleteToAdd
            animationDeleteToAdd.start()
            fabGate.animateBackgroundStateChange(colorRemove, colorAdd)
        }
        TransitionManager.beginDelayedTransition(fabGate.parent as ViewGroup)
    }

    override fun onBackPressed(): Boolean = viewModel.onBackPressed(this)

}