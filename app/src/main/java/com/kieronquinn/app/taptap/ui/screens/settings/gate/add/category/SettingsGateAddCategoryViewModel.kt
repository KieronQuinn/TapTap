package com.kieronquinn.app.taptap.ui.screens.settings.gate.add.category

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.kieronquinn.app.taptap.models.TapGateCategory
import com.kieronquinn.app.taptap.utils.extensions.navigate

class SettingsGateAddCategoryViewModel: ViewModel() {

    fun onCategoryClicked(fragment: Fragment, category: TapGateCategory) {
        fragment.navigate(SettingsGateAddCategoryFragmentDirections.actionSettingsGateAddCategoryFragmentToSettingsGateAddListFragment(category))
    }

}