package com.kieronquinn.app.taptap.ui.screens.settings.action.double

import com.kieronquinn.app.taptap.ui.screens.settings.action.SettingsActionFragment
import com.kieronquinn.app.taptap.ui.screens.settings.action.SettingsActionViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsDoubleTapActionFragment: SettingsActionFragment() {

    override val viewModel: SettingsActionViewModel by viewModel<SettingsDoubleTapActionViewModel>()

}