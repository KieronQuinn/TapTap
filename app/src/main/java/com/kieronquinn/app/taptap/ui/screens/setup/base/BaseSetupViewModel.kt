package com.kieronquinn.app.taptap.ui.screens.setup.base

import androidx.lifecycle.ViewModel

abstract class BaseSetupViewModel: ViewModel() {

    open fun onBackPressed(): Boolean {
        return false
    }

}