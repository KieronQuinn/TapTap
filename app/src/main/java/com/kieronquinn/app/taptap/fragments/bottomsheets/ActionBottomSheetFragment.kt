package com.kieronquinn.app.taptap.fragments.bottomsheets

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
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
import com.kieronquinn.app.taptap.fragments.BaseActionFragment
import com.kieronquinn.app.taptap.fragments.action.ActionListFragment
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.ActionDataTypes
import com.kieronquinn.app.taptap.utils.*
import dev.chrisbanes.insetter.Insetter
import kotlinx.android.synthetic.main.fragment_bottomsheet_action.*
import net.dinglisch.android.tasker.TaskerIntent

class ActionBottomSheetFragment : BottomSheetDialogFragment(), NavController.OnDestinationChangedListener {

    companion object {
        private const val REQUEST_CODE_SELECT_APP = 1001
        private const val REQUEST_CODE_PERMISSION = 1002
        private const val REQUEST_CODE_TASKER_ACTION = 1003
        private const val REQUEST_CODE_SHORTCUT = 1004
        private const val REQUEST_CODE_SHORTCUT_SECOND = 1005
    }

    private val isNotificationAccessGranted: Boolean
        get() {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return notificationManager.isNotificationPolicyAccessGranted
        }

    private var storedAction: ActionInternal? = null
    private var storedActionCallback: ((ActionInternal) -> Unit)? = null
    private var isWaitingForNotificationPermission = false

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
        return inflater.inflate(R.layout.fragment_bottomsheet_action, container, false)
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
        val actionListFragment = navHostFragment.childFragmentManager.fragments.firstOrNull() as? ActionListFragment
        actionListFragment?.let {
            bs_toolbar_title.text = it.getToolbarTitle()
            it.setToolbarListener { elevationEnabled ->
                setToolbarElevationEnabled(elevationEnabled)
            }
            it.setItemClickListener { action ->
                val dataType = action.action.dataType
                if(dataType != null){
                    //Fire off to the required data picker
                    launchDataPicker(action, dataType){ completedAction ->
                        if(dataType == ActionDataTypes.TASKER_TASK){
                            //Check Tasker permission too (this can be done synchronously)
                            checkTaskerAccessPermission()
                        }
                        val bundle = Bundle()
                        bundle.putParcelable(BaseActionFragment.addResultKey, completedAction)
                        setFragmentResult(BaseActionFragment.addResultKey, bundle)
                        dismiss()
                    }
                }else {
                    val bundle = Bundle()
                    bundle.putParcelable(BaseActionFragment.addResultKey, action)
                    setFragmentResult(BaseActionFragment.addResultKey, bundle)
                    dismiss()
                }
            }
        }
    }

    private fun checkTaskerAccessPermission() {
        if(TaskerIntent.testStatus(context) == TaskerIntent.Status.AccessBlocked){
            //User does not have Misc > Allow External Access enabled
            TaskerPermissionBottomSheetFragment().show(parentFragmentManager, "bs_tasker")
        }
    }

    private fun launchDataPicker(action: ActionInternal, dataTypes: ActionDataTypes, callback: (ActionInternal) -> Unit){
        storedActionCallback = callback
        storedAction = action
        when(dataTypes){
            ActionDataTypes.PACKAGE_NAME -> {
                startActivityForResult(Intent(requireContext(), AppPickerActivity::class.java), REQUEST_CODE_SELECT_APP)
            }
            ActionDataTypes.CAMERA_PERMISSION -> {
                if(context?.checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSION)
                }else{
                    callback.invoke(action)
                    storedActionCallback = null
                    storedAction = null
                }
            }
            ActionDataTypes.TASKER_TASK -> {
                try {
                    val intent = TaskerIntent.getTaskSelectIntent()
                    startActivityForResult(intent, REQUEST_CODE_TASKER_ACTION)
                }catch (e: Exception){
                    Toast.makeText(context, getString(R.string.action_tasker_event_toast), Toast.LENGTH_LONG).show()
                    storedActionCallback = null
                    storedAction = null
                }
            }
            ActionDataTypes.SHORTCUT -> {
                launchShortcutPicker(action, callback)
            }
            ActionDataTypes.ACCESS_NOTIFICATION_POLICY -> {
                if(!isNotificationAccessGranted) {
                    isWaitingForNotificationPermission = true
                    NotificationPolicyBottomSheetFragment().show(childFragmentManager, "bs_notification_policy")
                }else{
                    callback.invoke(action)
                    storedActionCallback = null
                    storedAction = null
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: Boolean = if(requestCode == REQUEST_CODE_SELECT_APP && resultCode == Activity.RESULT_OK){
            val currentAction = storedAction ?: return
            val packageName = data?.getStringExtra(AppsFragment.KEY_SELECTED_APP)
            if(packageName.isNullOrEmpty()) return
            currentAction.data = packageName
            storedActionCallback?.invoke(currentAction)
            true
        }else if(requestCode == REQUEST_CODE_TASKER_ACTION && resultCode == Activity.RESULT_OK){
            val currentAction = storedAction ?: return
            val taskName = data?.dataString ?: return
            currentAction.data = taskName
            storedActionCallback?.invoke(currentAction)
            true
        }else if(requestCode == REQUEST_CODE_SHORTCUT && resultCode == Activity.RESULT_OK){
            if(data?.component != null){
                //Handle the required extra step
                startActivityForResult(data, REQUEST_CODE_SHORTCUT_SECOND)
                false
            }else {
                handleShortcutIntent(data)
                true
            }
        }else if(requestCode == REQUEST_CODE_SHORTCUT_SECOND && resultCode == Activity.RESULT_OK){
            handleShortcutIntent(data)
            true
        }else false
        if(result) {
            storedActionCallback = null
            storedAction = null
        }
    }

    override fun onResume() {
        super.onResume()
        //startActivityForResult & onActivityResult don't seem to work for the policy permissions screen as it fires off a second activity and thus briefly returns to the app before it's done
        if(isWaitingForNotificationPermission && isNotificationAccessGranted){
            val storedAction = this.storedAction ?: return
            storedActionCallback?.invoke(storedAction)
            this.storedAction = null
            storedActionCallback = null
            isWaitingForNotificationPermission = false
        }
    }

    private fun handleShortcutIntent(data: Intent?) {
        val returnedIntent = data?.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
        val currentAction = storedAction ?: return
        val serializedIntent = returnedIntent?.serialize()
        if (serializedIntent != null) {
            currentAction.data = serializedIntent
            storedActionCallback?.invoke(currentAction)
        } else {
            Toast.makeText(
                context,
                getString(R.string.action_launch_shortcut_toast),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSION && grantResults.all { it == PackageManager.PERMISSION_GRANTED }){
            storedActionCallback?.invoke(storedAction ?: return)
            storedActionCallback = null
            storedAction = null
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

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        bs_toolbar_title.text = destination.label
    }

    private fun onBackPressed(){
        if(!navController.navigateUp()) dismiss()
    }

    private fun launchShortcutPicker(action: ActionInternal, callback: (ActionInternal) -> Unit){
        storedAction = action
        storedActionCallback = callback
        val intent = Intent(Intent.ACTION_PICK_ACTIVITY).apply {
            putExtra(Intent.EXTRA_INTENT, Intent(Intent.ACTION_CREATE_SHORTCUT))
        }
        startActivityForResult(intent, REQUEST_CODE_SHORTCUT)
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