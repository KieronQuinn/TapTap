package com.kieronquinn.app.taptap.ui.screens.settings.gate.add

import android.animation.ValueAnimator
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentBottomsheetGateBinding
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.utils.*
import com.kieronquinn.app.taptap.utils.extensions.*
import dev.chrisbanes.insetter.Insetter
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsGateAddContainerBottomSheetFragment: BottomSheetDialogFragment() {

    private var binding by autoCleared<FragmentBottomsheetGateBinding>()
    private val viewModel by sharedViewModel<SettingsGateAddContainerBottomSheetViewModel>()
    private val arguments by navArgs<SettingsGateAddContainerBottomSheetFragmentArgs>()

    private val navHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.bs_nav_host_fragment) as NavHostFragment
    }

    private val navController by lazy {
        navHostFragment.navController
    }

    private val avdBackToClose by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.avd_back_to_close) as AnimatedVectorDrawable
    }

    private val avdCloseToback by lazy {
        ContextCompat.getDrawable(requireContext(), R.drawable.avd_close_to_back) as AnimatedVectorDrawable
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBottomsheetGateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.isWhenGateFlow = arguments.isWhenGateFlow
        viewModel.currentGates = arguments.currentGates?.gates
        with(binding){
            bsToolbarNavigation.setOnClickListener {
                onBackPressed()
            }
        }
        with(viewModel){
            clearGate()
            backState.observe(viewLifecycleOwner){
                when(it!!){
                    SettingsGateAddContainerBottomSheetViewModel.BackState.CLOSE -> {
                        binding.bsToolbarNavigation.setImageDrawable(avdBackToClose)
                        avdBackToClose.start()
                        binding.bsToolbar.setToolbarElevationEnabled(false)
                    }
                    SettingsGateAddContainerBottomSheetViewModel.BackState.BACK -> {
                        binding.bsToolbarNavigation.setImageDrawable(avdCloseToback)
                        avdCloseToback.start()
                    }
                }
            }
            toolbarState.observe(viewLifecycleOwner){
                binding.bsToolbar.setToolbarElevationEnabled(it)
            }
            toolbarTitle.observe(viewLifecycleOwner){
                binding.bsToolbarTitle.setText(it)
            }
            gate.asLiveData().observe(viewLifecycleOwner){
                if(it != null) {
                    dismissWithGate(it)
                }
            }
        }
        navHostFragment.childFragmentManager.addOnBackStackChangedListener {
            val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            TransitionManager.beginDelayedTransition(bottomSheet, TransitionSet().addTransition(
                ChangeBounds()
            ))
        }
    }

    private fun dismissWithGate(gate: GateInternal){
        viewModel.setResultGate(this, gate)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener { _ ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            bottomSheet.fitsSystemWindows = false
            bottomSheet.setOnApplyWindowInsetsListener { v, insets ->
                bottomSheet.layoutParams.apply {
                    this as CoordinatorLayout.LayoutParams
                    topMargin = insets.stableInsetTop
                }
                bottomSheet.post {
                    BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                }
                insets
            }
        }
        dialog.setOnKeyListener { _, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP){
                onBackPressed()
            }
            true
        }
        return dialog
    }

    private fun onBackPressed(){
        if(!navController.navigateUp()) dismiss()
    }

    private var isToolbarElevationEnabled = false
    private var toolbarColorAnimation: ValueAnimator? = null
    private var toolbarElevationAnimation: ValueAnimator? = null

    private fun Toolbar.setToolbarElevationEnabled(enabled: Boolean){
        if(enabled == isToolbarElevationEnabled) return
        isToolbarElevationEnabled = enabled
        val toolbarColor = if(enabled){
            ContextCompat.getColor(context, R.color.toolbarColor)
        }else{
            ContextCompat.getColor(context, android.R.color.transparent)
        }
        val elevation = if(enabled) context.dip(8).toFloat() else 0f
        val initialBeforeColor = if(toolbarColorAnimation == null){
            ContextCompat.getColor(context, R.color.toolbarColor)
        }else{
            null
        }

        toolbarColorAnimation?.cancel()
        toolbarElevationAnimation?.cancel()
        toolbarColorAnimation = animateColorChange(beforeColor = initialBeforeColor, afterColor = toolbarColor)
        toolbarElevationAnimation = animateElevationChange(elevation)

        //Animate the corner radius of the background
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        val shapeBackground = bottomSheet.background as GradientDrawable
        val cornerRadius = resources.getDimension(R.dimen.bottom_sheet_corner_radius)
        if(enabled) {
            ValueAnimator.ofFloat(cornerRadius, 0f).apply {
                duration = 250
                addUpdateListener {
                    shapeBackground.cornerRadius = it.animatedValue as Float
                }
                start()
            }
        }else{
            ValueAnimator.ofFloat(0f, cornerRadius).apply {
                duration = 250
                addUpdateListener {
                    val animatedRadius = it.animatedValue as Float
                    shapeBackground.cornerRadii = arrayOf(animatedRadius, animatedRadius, animatedRadius, animatedRadius, 0f, 0f, 0f, 0f).toFloatArray()
                }
                start()
            }
        }
    }

    //Hacks to make the bottom sheet draw below the nav bar
    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.findViewById<View>(com.google.android.material.R.id.container).fitsSystemWindows = false
                window.decorView.run {
                    //systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    Insetter.setEdgeToEdgeSystemUiFlags(this, true)
                }
            }
            //Fix the sheet drawing behind the status bar
            window.findViewById<View>(com.google.android.material.R.id.coordinator).setOnApplyWindowInsetsListener { v, insets ->
                v.layoutParams.apply {
                    this as FrameLayout.LayoutParams
                    topMargin = insets.systemWindowInsetTop
                }
                insets
            }
        }

    }

    override fun getTheme(): Int {
        activity?.let {
            return if(it.isDarkTheme()) R.style.BaseBottomSheetDialog_Dark
            else R.style.BaseBottomSheetDialog
        }
        return R.style.BaseBottomSheetDialog
    }

}