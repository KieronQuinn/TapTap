package com.kieronquinn.app.taptap.ui.screens.settings.action.add.list.permissionsheets

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.isActionDataSatisfied
import com.kieronquinn.app.taptap.utils.extensions.toActivityDestination
import com.kieronquinn.app.taptap.utils.extensions.withStandardAnimations
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.settings.action.add.SettingsActionAddContainerBottomSheetViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsActionAddNotificationPolicyBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val sharedViewModel by sharedViewModel<SettingsActionAddContainerBottomSheetViewModel>()
    private val arguments by navArgs<SettingsActionAddGestureServiceBottomSheetFragmentArgs>()

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.bs_tasker_title)
        message(R.string.bs_notification_policy_content)
        noAutoDismiss()
        positiveButton(R.string.notification_policy_grant){
            runCatching {
                ActivityNavigator(requireContext()).navigate(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.toActivityDestination(context), null, NavOptions.Builder().withStandardAnimations().build(), null)
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