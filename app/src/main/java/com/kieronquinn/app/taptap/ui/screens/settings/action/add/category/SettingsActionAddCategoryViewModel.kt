package com.kieronquinn.app.taptap.ui.screens.settings.action.add.category

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.kieronquinn.app.taptap.models.TapActionCategory
import com.kieronquinn.app.taptap.utils.extensions.navigate

class SettingsActionAddCategoryViewModel: ViewModel() {

    fun onCategoryClicked(fragment: Fragment, category: TapActionCategory) {
        fragment.navigate(SettingsActionAddCategoryFragmentDirections.actionActionCategoryFragmentToActionListFragment(category))
    }

}