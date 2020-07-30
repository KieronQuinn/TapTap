package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.AppPickerActivity
import com.kieronquinn.app.taptap.fragments.AppsFragment
import com.kieronquinn.app.taptap.fragments.SettingsGateFragment
import com.kieronquinn.app.taptap.fragments.gate.GateListFragment
import com.kieronquinn.app.taptap.models.ActionDataTypes
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.GateDataTypes
import com.kieronquinn.app.taptap.models.GateInternal
import com.kieronquinn.app.taptap.utils.animateColorChange
import com.kieronquinn.app.taptap.utils.animateElevationChange
import com.kieronquinn.app.taptap.utils.dip
import com.kieronquinn.app.taptap.utils.isDarkTheme
import dev.chrisbanes.insetter.Insetter
import kotlinx.android.synthetic.main.fragment_bottomsheet_action.*

class GateBottomSheetFragment : BottomSheetDialogFragment(), NavController.OnDestinationChangedListener {

    companion object {
        private const val REQUEST_CODE_SELECT_APP = 1001
    }

    private var storedGate: GateInternal? = null
    private var storedGateCallback: ((GateInternal) -> Unit)? = null

    private val navHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.bs_nav_host_fragment) as NavHostFragment
    }

    private val navController by lazy {
        navHostFragment.navController
    }

    override fun getTheme(): Int {
        activity?.let {
            return if(it.isDarkTheme()) R.style.BaseBottomSheetDialog_Dark
            else R.style.BaseBottomSheetDialog
        }
        return R.style.BaseBottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener { _ ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            bottomSheet.fitsSystemWindows = false
            bottomSheet.setOnApplyWindowInsetsListener { v, insets ->
                Log.d("Insets", "Bottom sheet insets apply")
                bottomSheet.layoutParams.apply {
                    this as CoordinatorLayout.LayoutParams
                    topMargin = insets.stableInsetTop
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottomsheet_gate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController.addOnDestinationChangedListener(this)
        bs_toolbar.apply {
            navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_close)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }
        navHostFragment.childFragmentManager.addOnBackStackChangedListener {
            if (navHostFragment.childFragmentManager.backStackEntryCount == 0) {
                bs_toolbar.navigationIcon = ContextCompat.getDrawable(bs_toolbar.context, R.drawable.ic_close)
                setToolbarElevationEnabled(false)
            } else {
                bs_toolbar.navigationIcon = ContextCompat.getDrawable(bs_toolbar.context, R.drawable.ic_back)
            }
            val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            TransitionManager.beginDelayedTransition(bottomSheet, TransitionSet().addTransition(ChangeBounds()))
            trySetupFragment()
        }
        view.post {
            trySetupFragment()
        }
    }

    private fun trySetupFragment(){
        val gateListFragment = navHostFragment.childFragmentManager.fragments.firstOrNull() as? GateListFragment
        gateListFragment?.let {
            bs_toolbar_title.text = it.getToolbarTitle()
            it.setToolbarListener { elevationEnabled ->
                setToolbarElevationEnabled(elevationEnabled)
            }
            it.setItemClickListener { gate ->
                val dataType = gate.gate.dataType
                if(dataType != null){
                    //Fire off to data picker
                    launchDataPicker(gate, dataType){ completedGate ->
                        val bundle = Bundle()
                        bundle.putParcelable(SettingsGateFragment.addResultKey, completedGate)
                        setFragmentResult(SettingsGateFragment.addResultKey, bundle)
                        dismiss()
                    }
                }else {
                    val bundle = Bundle()
                    bundle.putParcelable(SettingsGateFragment.addResultKey, gate)
                    setFragmentResult(SettingsGateFragment.addResultKey, bundle)
                    dismiss()
                }
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

    private fun launchDataPicker(gate: GateInternal, dataTypes: GateDataTypes, callback: (GateInternal) -> Unit){
        storedGateCallback = callback
        storedGate = gate
        when(dataTypes){
            GateDataTypes.PACKAGE_NAME -> {
                startActivityForResult(
                    Intent(requireContext(), AppPickerActivity::class.java),
                    REQUEST_CODE_SELECT_APP
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_CODE_SELECT_APP && resultCode == Activity.RESULT_OK){
            val currentGate = storedGate ?: return
            val packageName = data?.getStringExtra(AppsFragment.KEY_SELECTED_APP)
            if(packageName.isNullOrEmpty()) return
            currentGate.data = packageName
            storedGateCallback?.invoke(currentGate)
            storedGateCallback = null
            storedGate = null
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        bs_toolbar_title.text = destination.label
    }

    private fun onBackPressed(){
        if(!navController.navigateUp()) dismiss()
    }

    private var isToolbarElevationEnabled = false
    private var toolbarColorAnimation: ValueAnimator? = null
    private var toolbarElevationAnimation: ValueAnimator? = null

    private fun setToolbarElevationEnabled(enabled: Boolean){
        val context = bs_toolbar.context
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
        toolbarColorAnimation = bs_toolbar.animateColorChange(beforeColor = initialBeforeColor, afterColor = toolbarColor)
        toolbarElevationAnimation = bs_toolbar.animateElevationChange(elevation)

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

}