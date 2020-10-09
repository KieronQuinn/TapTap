package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.app.Dialog
import android.os.Bundle
import android.os.Parcel
import android.text.TextUtils
import android.widget.TextView
import androidx.core.view.doOnNextLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.bottomsheets.expandBottomSheet
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_MODEL
import com.kieronquinn.app.taptap.utils.applyTapTheme
import com.kieronquinn.app.taptap.utils.sharedPreferences

open class MaterialBottomSheetDialogFragment: DialogFragment() {

    companion object {

        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"

        fun create(instance: MaterialBottomSheetDialogFragment, fragmentManager: FragmentManager, tag: String, dialogCallback: (MaterialDialog) -> Unit){
            runCatching {
                instance.apply {
                    setListener {
                        dialogCallback.invoke(it)
                        it.view.doOnNextLayout { _ ->
                            it.expandBottomSheet()
                        }
                    }
                }.show(fragmentManager, tag)
            }
        }
    }

    private var dialogListener: ((MaterialDialog) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT)).applyTapTheme().show {
            //Default to OK
            positiveButton(android.R.string.ok)
            savedInstanceState?.getCharSequence(KEY_TITLE)?.let {
                title(text = it.toString())
            }
            savedInstanceState?.getCharSequence(KEY_MESSAGE)?.let {
                message(text = it)
            }
            dialogListener?.invoke(this)
            setupFragment(this)
            setupFragment(this, savedInstanceState)
        }
    }

    open fun setupFragment(dialog: MaterialDialog){}

    open fun setupFragment(dialog: MaterialDialog, savedInstanceState: Bundle?){}

    fun setListener(listener: ((MaterialDialog) -> Unit)?){
        this.dialogListener = listener
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            val title = dialog?.findViewById<TextView>(R.id.md_text_title)?.text
            val message = dialog?.findViewById<TextView>(R.id.md_text_message)?.text
            putCharSequence(KEY_TITLE, title)
            putCharSequence(KEY_MESSAGE, message)
        }
    }

}