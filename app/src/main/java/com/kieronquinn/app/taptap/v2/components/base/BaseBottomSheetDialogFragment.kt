package com.kieronquinn.app.taptap.v2.components.base

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.applyTapTheme

abstract class BaseBottomSheetDialogFragment: BottomSheetDialogFragment() {

    abstract fun onMaterialDialogCreated(materialDialog: MaterialDialog): MaterialDialog

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        onMaterialDialogCreated(MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).applyTapTheme())

    internal fun MaterialDialog.withOk() {
        positiveButton(android.R.string.ok){ dismiss() }
    }

}