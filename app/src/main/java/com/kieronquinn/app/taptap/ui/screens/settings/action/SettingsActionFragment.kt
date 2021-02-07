package com.kieronquinn.app.taptap.ui.screens.settings.action

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
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.databinding.FragmentActionsBinding
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.ui.screens.settings.action.triple.SettingsTripleTapActionFragment
import com.kieronquinn.app.taptap.utils.extensions.animateBackgroundStateChange
import com.kieronquinn.app.taptap.utils.extensions.observe
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin

abstract class SettingsActionFragment: BoundFragment<FragmentActionsBinding>(FragmentActionsBinding::class.java) {

    abstract val viewModel: SettingsActionViewModel

    private val _viewModel
        get() = viewModel

    private val adapter by lazy {
        SettingsActionAdapter(viewLifecycleOwner, _viewModel, requireContext(), emptyList<ActionInternal>().toMutableList(), this is SettingsTripleTapActionFragment){
            viewModel.onHeaderClicked(this)
        }.apply {
            chipAddClickListener = {
                viewModel.navigateToAddGateDialog(this@SettingsActionFragment, it, this)
            }
        }
    }

    private val animationAddToDelete by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.ic_add_to_delete) as AnimatedVectorDrawable
    }

    private val animationDeleteToAdd by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_to_add) as AnimatedVectorDrawable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(_viewModel){
            state.observe(viewLifecycleOwner){
                when(it){
                    is SettingsActionViewModel.State.Loading -> {
                        binding.loadingState.isVisible = true
                        binding.emptyState.isVisible = false
                    }
                    is SettingsActionViewModel.State.Loaded -> {
                        binding.loadingState.isVisible = false
                        binding.emptyState.isVisible = false
                        val isAddedItem = adapter.itemCount != 1 && it.actions.size + 1 > adapter.itemCount
                        adapter.items = it.actions
                        adapter.notifyDataSetChanged()
                        if(isAddedItem){
                            binding.recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                        }
                    }
                    is SettingsActionViewModel.State.Empty -> {
                        binding.emptyState.isVisible = true
                    }
                }
            }
            fabState.observe(viewLifecycleOwner){
                binding.setFabState(it)
            }
        }

        with(binding){
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter
            _viewModel.getItemTouchHelper(adapter).attachToRecyclerView(recyclerView)
            fabAction.applySystemWindowInsetsToMargin(bottom = true)
            fabAction.setOnClickListener {
                _viewModel.onFabClicked(this@SettingsActionFragment, adapter)
            }
            fabAction.post {
                recyclerView.bindToInsets(fabAction.measuredHeight + fabAction.marginBottom)
            }
        }

        _viewModel.getActions()
    }

    private fun FragmentActionsBinding.setFabState(removeEnabled: Boolean) {
        val colorRemove = ContextCompat.getColor(fabAction.context, R.color.fab_color_delete)
        val colorAdd = ContextCompat.getColor(fabAction.context, R.color.fab_color)
        if (removeEnabled) {
            fabAction.text = fabAction.context.getString(R.string.fab_remove_action)
            fabAction.icon = animationAddToDelete
            animationAddToDelete.start()
            fabAction.animateBackgroundStateChange(colorAdd, colorRemove)
        } else {
            fabAction.text = fabAction.context.getString(R.string.fab_add_action)
            fabAction.icon = animationDeleteToAdd
            animationDeleteToAdd.start()
            fabAction.animateBackgroundStateChange(colorRemove, colorAdd)
        }
        TransitionManager.beginDelayedTransition(fabAction.parent as ViewGroup)
    }

    override fun onBackPressed(): Boolean = _viewModel.onBackPressed(this)

}