package com.kieronquinn.app.taptap.fragments.bottomsheets

import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_MODEL
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.getCurrentTapModel
import com.kieronquinn.app.taptap.models.getDefaultTfModel
import com.kieronquinn.app.taptap.utils.sharedPreferences

class SettingsGestureModelDialogFragment: MaterialBottomSheetDialogFragment() {

    override fun setupFragment(dialog: MaterialDialog) {
        super.setupFragment(dialog)
        dialog.apply {
            title(R.string.setting_gesture_model)
            positiveButton(android.R.string.ok)
            negativeButton(android.R.string.cancel)
            val currentlySelected = context.getCurrentTapModel()
            val values = TfModel.values().map { it }
            val currentlySelectedIndex = values.indexOf(currentlySelected)
            listItemsSingleChoice(R.array.device_model_keys, initialSelection = currentlySelectedIndex){ dialog, index, text ->
                val value = values[index]
                sharedPreferences?.edit()?.putString(SHARED_PREFERENCES_KEY_MODEL, value.name)?.apply()
            }
        }
    }

}