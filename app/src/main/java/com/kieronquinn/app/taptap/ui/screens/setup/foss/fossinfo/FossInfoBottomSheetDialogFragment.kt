package com.kieronquinn.app.taptap.ui.screens.setup.foss.fossinfo

import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment

class FossInfoBottomSheetDialogFragment: BaseBottomSheetDialogFragment() {

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.setup_foss_info_bs_title)
        message(R.string.setup_foss_info_bs_content)
        withOk()
    }

}