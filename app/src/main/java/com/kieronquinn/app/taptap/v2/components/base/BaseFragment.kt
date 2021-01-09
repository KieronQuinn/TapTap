package com.kieronquinn.app.taptap.v2.components.base

import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {

    open fun onBackPressed(): Boolean {
        return false
    }

    open fun onHomeAsUpPressed(): Boolean {
        return false
    }

}