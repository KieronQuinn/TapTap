package com.kieronquinn.app.taptap.v2.components.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.utils.autoCleared

abstract class BoundFragment<T: ViewBinding>(private val viewBindingClass: Class<T>) : BaseFragment() {

    internal var binding by autoCleared<T>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = viewBindingClass.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java).invoke(null, inflater, container, false) as T
        return binding.root
    }

}