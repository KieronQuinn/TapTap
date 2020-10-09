package com.kieronquinn.app.taptap.fragments.setup

import androidx.fragment.app.Fragment

open class BaseSetupFragment: Fragment() {

    open fun onBackPressed(): Boolean {
        return false
    }

    open fun onHomeAsUpPressed(): Boolean {
        return false
    }

}