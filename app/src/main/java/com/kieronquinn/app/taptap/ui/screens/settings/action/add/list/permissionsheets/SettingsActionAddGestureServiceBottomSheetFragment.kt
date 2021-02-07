package com.kieronquinn.app.taptap.ui.screens.settings.action.add.list.permissionsheets

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.services.TapGestureAccessibilityService
import com.kieronquinn.app.taptap.models.isActionDataSatisfied
import com.kieronquinn.app.taptap.utils.extensions.EXTRA_FRAGMENT_ARG_KEY
import com.kieronquinn.app.taptap.utils.extensions.EXTRA_SHOW_FRAGMENT_ARGUMENTS
import com.kieronquinn.app.taptap.utils.extensions.toActivityDestination
import com.kieronquinn.app.taptap.utils.extensions.withStandardAnimations
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsActionAddGestureServiceBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val sharedViewModel by sharedViewModel<SettingsActionAddContainerBottomSheetViewModel>()
    private val arguments by navArgs<SettingsActionAddGestureServiceBottomSheetFragmentArgs>()

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.bs_secondary_service_title)
        message(R.string.bs_secondary_service_content)
        noAutoDismiss()
        positiveButton(R.string.notification_policy_grant){
            runCatching {
                ActivityNavigator(requireContext()).navigate(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    val bundle = Bundle()
                    val componentName = ComponentName(BuildConfig.APPLICATION_ID, TapGestureAccessibilityService::class.java.name).flattenToString()
                    bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
                    putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
                    putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
                }.toActivityDestination(requireContext()), null, NavOptions.Builder().withStandardAnimations().build(), null)
                Toast.makeText(it.context, R.string.accessibility_info_toast_gesture, Toast.LENGTH_LONG).show()
            }
        }
        negativeButton(android.R.string.cancel){
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        if(arguments.action.isActionDataSatisfied(requireContext())){
            sharedViewModel.addAction(arguments.action)
            findNavController().navigateUp()
        }
    }

}