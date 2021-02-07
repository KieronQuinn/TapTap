package com.kieronquinn.app.taptap.ui.screens.settings.action.add.category

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentAddActionCategoryBinding
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsActionAddCategoryFragment: BoundFragment<FragmentAddActionCategoryBinding>(FragmentAddActionCategoryBinding::class.java) {

    private val viewModel by viewModel<SettingsActionAddCategoryViewModel>()
    private val containerViewModel by sharedViewModel<SettingsActionAddContainerBottomSheetViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        containerViewModel.toolbarTitle.update(R.string.fab_add_action)
        containerViewModel.backState.update(SettingsActionAddContainerBottomSheetViewModel.BackState.CLOSE)
        with(binding.root){
            adapter = SettingsActionAddCategoryAdapter(context){
                viewModel.onCategoryClicked(this@SettingsActionAddCategoryFragment, it)
            }
            layoutManager = GridLayoutManager(context, 2)
            applySystemWindowInsetsToPadding(bottom = true)
        }
    }

}