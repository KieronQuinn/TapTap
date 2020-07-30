package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.sharedPreferences
import kotlinx.android.synthetic.main.fragment_bottomsheet_radio_buttons.*
import kotlinx.android.synthetic.main.item_radio_button.view.*

class RadioButtonBottomSheetFragment : BottomSheetFragment(), CompoundButton.OnCheckedChangeListener {

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_KEYS = "keys"
        const val KEY_VALUES = "values"
        const val KEY_SELECTED_INDEX = "selected_index"
        const val KEY_PREFERENCE_KEY = "preference_key"
        private const val KEY_SELECTED_INDEX_RESTORE = "selected_index_restore"
    }

    private val title by lazy { arguments?.get(KEY_TITLE) as CharSequence }
    private val keys by lazy { arguments?.get(KEY_KEYS) as Array<*> }
    private val values by lazy { arguments?.get(KEY_VALUES) as Array<*> }
    private val selectedIndex by lazy { arguments?.get(KEY_SELECTED_INDEX) as Int }
    private val preferenceKey by lazy { arguments?.get(KEY_PREFERENCE_KEY) as String }

    init {
        layout = R.layout.fragment_bottomsheet_radio_buttons
        cancelListener = {true}
        okListener = {
            setPreference()
            true
        }
        isSwipeable = true
    }

    private val radioButtons = ArrayList<MaterialRadioButton>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        radioButtons.clear()
        bs_title.text = title
        bs_options.apply {
            for((index, key) in keys.withIndex()){
                val radioButtonContainer = layoutInflater.inflate(R.layout.item_radio_button, bs_options, false)
                radioButtonContainer.radioButton.apply {
                    text = key.toString()
                    setOnCheckedChangeListener(this@RadioButtonBottomSheetFragment)
                    radioButtons.add(this)
                }
                if(index == selectedIndex){
                    radioButtonContainer.radioButton.isChecked = true
                }
                bs_options.addView(radioButtonContainer)
            }
        }
        if(savedInstanceState?.containsKey(KEY_SELECTED_INDEX_RESTORE) == true){
            val restoredSelectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX_RESTORE)
            radioButtons.forEachIndexed { index, materialRadioButton ->
                materialRadioButton.post {
                    materialRadioButton.isChecked = index == restoredSelectedIndex
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_INDEX_RESTORE, radioButtons.indexOfFirst { it.isChecked })
    }

    private fun setPreference(){
        val selectedItem = radioButtons.indexOfFirst { it.isChecked }
        val value = values[selectedItem] as String
        sharedPreferences?.edit()?.putString(preferenceKey, value)?.apply()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if(!buttonView.isPressed) return
        radioButtons.forEach {
            it.isChecked = false
        }
        buttonView.isChecked = true
    }

}