package com.kieronquinn.app.taptap.ui.screens.settings.main.beta

import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment

class SettingsMainBetaBottomSheetDialogFragment: BaseBottomSheetDialogFragment() {

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.bs_beta_title)
        message(R.string.bs_beta)
        withOk()
    }

}