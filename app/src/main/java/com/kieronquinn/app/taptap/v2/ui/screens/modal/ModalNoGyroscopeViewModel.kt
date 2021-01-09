package com.kieronquinn.app.taptap.v2.ui.screens.modal

import com.kieronquinn.app.taptap.v2.components.base.BaseViewModel

class ModalNoGyroscopeViewModel: BaseViewModel() {

    fun onNextClicked(fragment: ModalNoGyroscopeFragment){
        fragment.activity?.finish()
    }

}