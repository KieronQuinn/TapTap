package com.kieronquinn.app.taptap.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.kieronquinn.app.taptap.utils.TransitionUtils
import com.kieronquinn.monetcompat.app.MonetFragment

abstract class BoundFragment<T: ViewBinding>(private val inflate: (LayoutInflater, ViewGroup?, Boolean) -> T): MonetFragment() {

    private var _binding: T? = null
    protected val binding: T
        get() = _binding ?: throw RuntimeException("Cannot access binding before onCreateView or after onDestroyView")

    open val disableEnterExitAnimation = false
    open val disableAllAnimation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!disableAllAnimation) {
            if(!disableEnterExitAnimation) {
                exitTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), true)
                enterTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), true)
            }
            returnTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), false)
            reenterTransition = TransitionUtils.getMaterialSharedAxis(requireContext(), false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}