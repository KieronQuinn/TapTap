package com.kieronquinn.app.taptap.components.base

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController

abstract class BaseViewModel: ViewModel() {

    open fun onBackPressed(fragment: Fragment): Boolean {
        if(!fragment.findNavController().navigateUp()) fragment.requireActivity().finish()
        return true
    }

}