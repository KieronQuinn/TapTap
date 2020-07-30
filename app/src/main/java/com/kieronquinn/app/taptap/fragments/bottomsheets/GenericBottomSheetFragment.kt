package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import com.kieronquinn.app.taptap.R
import kotlinx.android.synthetic.main.bottom_sheet_buttons.*
import kotlinx.android.synthetic.main.fragment_bottomsheet_generic.*

class GenericBottomSheetFragment : BottomSheetFragment() {

    companion object {
        const val KEY_MESSAGE = "message"
        const val KEY_TITLE = "title"
        const val KEY_POSITIVE_LABEL = "positive_label"

        fun create(message: CharSequence, @StringRes title: Int, @StringRes positiveLabel: Int): GenericBottomSheetFragment {
            val arguments = bundleOf(KEY_MESSAGE to message, KEY_TITLE to title, KEY_POSITIVE_LABEL to positiveLabel)
            return GenericBottomSheetFragment().apply {
                this.arguments = arguments
            }
        }
    }

    private var savedData: Bundle? = null

    init {
        layout = R.layout.fragment_bottomsheet_generic
        okListener = {true}
        isCancelable = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bundle = savedInstanceState ?: arguments ?: return
        savedData = bundle
        okLabel = bundle.getInt(KEY_POSITIVE_LABEL)
        val title = bundle.getInt(KEY_TITLE)
        val message = bundle.getCharSequence(KEY_MESSAGE, "")
        super.onViewCreated(view, savedInstanceState)
        text.text = message
        bs_toolbar_title.text = getString(title)
        bottom_sheet_ok.text = getString(okLabel!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(savedData)
    }

}