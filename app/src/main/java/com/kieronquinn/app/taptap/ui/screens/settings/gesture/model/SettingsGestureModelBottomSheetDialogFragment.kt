package com.kieronquinn.app.taptap.ui.screens.settings.gesture.model

import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.getCurrentTapModel
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsGestureModelBottomSheetDialogFragment: BaseBottomSheetDialogFragment() {

    private val viewModel by viewModel<SettingsGestureModelBottomSheetViewModel>()

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?) = materialDialog.apply {
        title(R.string.setting_gesture_model)
        positiveButton(android.R.string.ok)
        negativeButton(android.R.string.cancel)
        val currentlySelected = context.getCurrentTapModel()
        val values = TfModel.values().map { it }
        val currentlySelectedIndex = values.indexOf(currentlySelected)
        listItemsSingleChoice(R.array.device_model_keys, initialSelection = currentlySelectedIndex){ dialog, index, text ->
            val value = values[index]
            viewModel.onModelSelected(value)
        }
    }

}