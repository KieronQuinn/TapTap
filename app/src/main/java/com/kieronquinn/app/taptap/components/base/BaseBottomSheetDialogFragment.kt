package com.kieronquinn.app.taptap.components.base

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.taptap.utils.extensions.applyTapTheme

abstract class BaseBottomSheetDialogFragment: BottomSheetDialogFragment() {

    abstract fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog

    final override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        onMaterialDialogCreated(MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).applyTapTheme(), savedInstanceState)

    internal fun MaterialDialog.withOk() {
        positiveButton(android.R.string.ok){ dismiss() }
    }

}