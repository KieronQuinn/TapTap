package com.kieronquinn.app.taptap.ui.screens.modal

import com.kieronquinn.app.taptap.components.base.BaseViewModel

class ModalNoGyroscopeViewModel: BaseViewModel() {

    fun onNextClicked(fragment: ModalNoGyroscopeFragment){
        fragment.activity?.finish()
    }

}