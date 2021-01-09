package com.kieronquinn.app.taptap.v2.ui.screens.setup.foss.fossinfo

import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.v2.components.base.BaseBottomSheetDialogFragment

class FossInfoBottomSheetDialogFragment: BaseBottomSheetDialogFragment() {

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog): MaterialDialog = materialDialog.apply {
        title(R.string.setup_foss_info_bs_title)
        message(R.string.setup_foss_info_bs_content)
        withOk()
    }

}