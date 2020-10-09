package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.os.Bundle
import android.text.InputType
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_SENSITIVITY
import com.kieronquinn.app.taptap.utils.sharedPreferences
import kotlinx.android.synthetic.main.fragment_bottomsheet_input.*
import kotlinx.android.synthetic.main.fragment_bottomsheet_input.view.*

class CustomSensitivityBottomSheet : MaterialBottomSheetDialogFragment() {

    companion object {
        private const val KEY_CURRENT_VALUE = "current_value"
    }

    override fun setupFragment(dialog: MaterialDialog, savedInstanceState: Bundle?) {
        super.setupFragment(dialog)
        dialog.apply {
            title(R.string.bs_advanced_custom_sensitivity)
            customView(R.layout.fragment_bottomsheet_input)
            positiveButton(R.string.bs_advanced_custom_sensitivity_positive){
                //Check it's actually castable
                kotlin.runCatching {
                    val newValue = bs_input_edit_text.text!!.toString().toFloat().toString()
                    sharedPreferences?.edit()?.putString(SHARED_PREFERENCES_KEY_SENSITIVITY, newValue)?.apply()
                }
            }
            negativeButton(R.string.bs_advanced_custom_sensitivity_negative)
            neutralButton(R.string.bs_advanced_custom_sensitivity_neutral){
                sharedPreferences?.edit()?.putString(SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05")?.apply()
            }
            val currentValue = sharedPreferences?.getString(SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05")?.toFloatOrNull() ?: 0.05f
            getCustomView().run {
                bs_input_text_input.run {
                    hint = getString(R.string.bs_advanced_custom_sensitivity_hint)
                }
                bs_input_edit_text.run {
                    inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    savedInstanceState?.getString(KEY_CURRENT_VALUE)?.let {
                        editableText.clear()
                        append(it)
                    } ?: run {
                        editableText.clear()
                        append(currentValue.toString())
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        bs_input_edit_text?.text?.toString()?.let {
            outState.putString(KEY_CURRENT_VALUE, it)
        }
    }

}