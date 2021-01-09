package com.kieronquinn.app.taptap.v2.ui.screens.setup.configuration.troubleshooting

import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.v2.components.base.BaseBottomSheetDialogFragment

class TroubleshootingBottomSheetDialogFragment: BaseBottomSheetDialogFragment() {

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog): MaterialDialog = materialDialog.apply {
        title(R.string.bs_troubleshooting_title)
        message(R.string.bs_troubleshooting_content)
        withOk()
    }

}