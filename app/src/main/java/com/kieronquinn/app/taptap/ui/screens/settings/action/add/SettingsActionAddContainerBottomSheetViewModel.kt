package com.kieronquinn.app.taptap.ui.screens.settings.action.add

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.TapAction
import com.kieronquinn.app.taptap.utils.extensions.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SettingsActionAddContainerBottomSheetViewModel: ViewModel() {

    companion object {
        const val RESULT_KEY_ACTION = "action"
        const val REQUEST_KEY_ADD_ACTION = "add_action"
    }

    val scrollPosition = MutableLiveData<Int>()
    val toolbarState = MediatorLiveData<Boolean>().apply {
        addSource(scrollPosition){
            update(it > 0)
        }
    }
    val toolbarTitle = MutableLiveData<Int>()
    val backState = MutableLiveData(BackState.CLOSE)

    enum class BackState {
        CLOSE, BACK
    }

    val action = MutableStateFlow<ActionInternal?>(null)

    fun addAction(tapAction: TapAction, data: String? = null) = viewModelScope.launch {
        action.emit(ActionInternal(tapAction, ArrayList(), data))
    }

    fun clearAction() = viewModelScope.launch {
        action.emit(null)
    }

    fun setResultAction(fragment: BottomSheetDialogFragment, action: ActionInternal){
        fragment.setFragmentResult(REQUEST_KEY_ADD_ACTION, bundleOf(RESULT_KEY_ACTION to action))
        fragment.dismiss()
    }

    enum class ActionType {
        DOUBLE, TRIPLE
    }

}