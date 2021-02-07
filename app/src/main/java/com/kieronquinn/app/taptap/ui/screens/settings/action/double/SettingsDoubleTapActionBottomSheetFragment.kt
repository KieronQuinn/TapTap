package com.kieronquinn.app.taptap.ui.screens.settings.action.double

import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment

class SettingsDoubleTapActionBottomSheetFragment: BaseBottomSheetDialogFragment() {

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?) = materialDialog.apply {
        title(R.string.bs_help_action_title)
        message(R.string.bs_help_action)
        withOk()
    }

}