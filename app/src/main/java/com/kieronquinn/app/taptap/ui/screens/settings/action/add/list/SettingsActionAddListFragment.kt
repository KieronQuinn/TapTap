package com.kieronquinn.app.taptap.ui.screens.settings.action.add.list

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.databinding.FragmentAddActionListBinding
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsActionAddListFragment: BoundFragment<FragmentAddActionListBinding>(FragmentAddActionListBinding::class.java) {

    private val arguments by navArgs<SettingsActionAddListFragmentArgs>()
    private val viewModel by viewModel<SettingsActionAddListViewModel>()
    private val containerViewModel by sharedViewModel<SettingsActionAddContainerBottomSheetViewModel>()

    internal val permissionResultLiveData = MutableLiveData<Boolean?>(null)
    internal val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        lifecycleScope.launch {
            permissionResultLiveData.postValue(granted.all { it.value })
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
        with(containerViewModel){
            clearAction()
            toolbarTitle.update(arguments.category.labelRes)
            backState.update(SettingsActionAddContainerBottomSheetViewModel.BackState.BACK)
        }
        with(binding.root){
            adapter = SettingsActionAddListAdapter(context, viewModel.getActions(context, arguments.category)){
                viewModel.onActionClicked(this@SettingsActionAddListFragment, containerViewModel, it)
            }
            layoutManager = LinearLayoutManager(context)
            setOnApplyWindowInsetsListener { v, insets ->
                setPadding(0, paddingTop, 0, insets.systemWindowInsetBottom)
                insets
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.root.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        binding.root.removeOnScrollListener(scrollListener)
    }

}