package com.kieronquinn.app.taptap.utils

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController

fun Fragment.navigate(navDirections: NavDirections){
    findNavController().navigate(navDirections)
}