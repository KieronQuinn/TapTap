package com.kieronquinn.app.taptap.ui.screens.settings.gate.add.category

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentAddGateCategoryBinding
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.gate.add.SettingsGateAddContainerBottomSheetViewModel
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsGateAddCategoryFragment: BoundFragment<FragmentAddGateCategoryBinding>(FragmentAddGateCategoryBinding::class.java) {

    private val viewModel by viewModel<SettingsGateAddCategoryViewModel>()
    private val containerViewModel by sharedViewModel<SettingsGateAddContainerBottomSheetViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        containerViewModel.toolbarTitle.update(R.string.fab_add_gate)
        containerViewModel.backState.update(SettingsGateAddContainerBottomSheetViewModel.BackState.CLOSE)
        with(binding.root){
            adapter = SettingsGateAddCategoryAdapter(context){
                viewModel.onCategoryClicked(this@SettingsGateAddCategoryFragment, it)
            }
            layoutManager = GridLayoutManager(context, 2)
            applySystemWindowInsetsToPadding(bottom = true)
        }
    }

}