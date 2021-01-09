package com.kieronquinn.app.taptap.v2.components.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController

abstract class BaseViewModel: ViewModel() {

    open fun onBackPressed(fragment: Fragment): Boolean {
        fragment.findNavController().navigateUp()
        return true
    }

}