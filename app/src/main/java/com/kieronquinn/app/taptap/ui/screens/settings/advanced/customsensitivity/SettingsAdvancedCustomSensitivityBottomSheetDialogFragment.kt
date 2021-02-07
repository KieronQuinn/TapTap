package com.kieronquinn.app.taptap.ui.screens.settings.advanced.customsensitivity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentBottomsheetInputBinding
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsAdvancedCustomSensitivityBottomSheetDialogFragment: BaseBottomSheetDialogFragment() {

    companion object {
        private const val KEY_CURRENT_VALUE = "current_value"
    }

    private val textChangedListener = object: TextWatcher {

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.currentValue.postValue(s?.toString())
        }

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    }


    private val viewModel by viewModel<SettingsAdvancedCustomSensitivityBottomSheetDialogViewModel>()
    private lateinit var binding: FragmentBottomsheetInputBinding

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.bs_advanced_custom_sensitivity)
        customView(R.layout.fragment_bottomsheet_input)
        binding = FragmentBottomsheetInputBinding.bind(getCustomView())
        with(binding){
            bsInputTextInput.hint = getString(R.string.bs_advanced_custom_sensitivity_hint)
            viewModel.currentValue.observe(this@SettingsAdvancedCustomSensitivityBottomSheetDialogFragment){
                bsInputEditText.updateText(it)
            }
            bsInputEditText.addTextChangedListener(textChangedListener)
        }
        positiveButton(R.string.bs_advanced_custom_sensitivity_positive){
            viewModel.onPositiveButtonClicked(this@SettingsAdvancedCustomSensitivityBottomSheetDialogFragment)
        }
        neutralButton(R.string.bs_advanced_custom_sensitivity_neutral){
            viewModel.onNeutralButtonClicked(this@SettingsAdvancedCustomSensitivityBottomSheetDialogFragment)
        }
        negativeButton(R.string.bs_advanced_custom_sensitivity_negative)
    }

    private fun EditText.updateText(text: String){
        removeTextChangedListener(textChangedListener)
        this.text.run {
            clear()
            append(text)
        }
        addTextChangedListener(textChangedListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.bsInputTextInput.editText?.text?.toString()?.let {
            outState.putString(KEY_CURRENT_VALUE, it)
        }
    }

}