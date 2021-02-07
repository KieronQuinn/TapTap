package com.kieronquinn.app.taptap.ui.screens.settings.gate.add.list

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.FragmentAddGateListBinding
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.SettingsGateAddContainerBottomSheetViewModel
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsGateAddListFragment: BoundFragment<FragmentAddGateListBinding>(FragmentAddGateListBinding::class.java) {

    private val containerViewModel by sharedViewModel<SettingsGateAddContainerBottomSheetViewModel>()
    private val viewModel by viewModel<SettingsGateAddListViewModel>()
    private val arguments by navArgs<SettingsGateAddListFragmentArgs>()

    internal val permissionResultLiveData = MutableLiveData<Boolean?>(null)
    internal val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        lifecycleScope.launch {
            permissionResultLiveData.postValue(it)
        }
    }

    internal val activityResultLiveData = MutableLiveData<ActivityResult?>(null)
    internal val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        lifecycleScope.launch {
            activityResultLiveData.postValue(it)
        }
    }

    private val scrollListener = object: RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            containerViewModel.scrollPosition.update(recyclerView.computeVerticalScrollOffset())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val category = arguments.category
        with(binding){
            val availableGates = viewModel.getAvailableGates(requireContext(), category, containerViewModel.currentGates)
            if(availableGates.isNotEmpty()) {
                recyclerView.adapter = SettingsGateAddListAdapter(requireContext(),
                    containerViewModel.isWhenGateFlow, availableGates){
                    viewModel.onGateClicked(this@SettingsGateAddListFragment, containerViewModel, it)
                }
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }else{
                recyclerView.isVisible = false
                emptyState.isVisible = true
            }
            recyclerView.doOnApplyWindowInsets { _, insets, _ ->
                recyclerView.setPadding(0, recyclerView.paddingTop, 0, insets.systemWindowInsetBottom)
            }
            emptyState.doOnApplyWindowInsets { _, insets, _ ->
                emptyState.setPadding(0, emptyState.paddingTop, 0, insets.systemWindowInsetBottom)
            }
        }
        with(containerViewModel){
            toolbarTitle.update(category.labelRes)
            backState.update(SettingsGateAddContainerBottomSheetViewModel.BackState.BACK)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerView.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        binding.recyclerView.removeOnScrollListener(scrollListener)
    }

}