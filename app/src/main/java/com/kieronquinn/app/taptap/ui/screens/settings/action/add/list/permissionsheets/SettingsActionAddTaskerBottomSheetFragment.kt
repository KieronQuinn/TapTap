package com.kieronquinn.app.taptap.ui.screens.settings.action.add.list.permissionsheets

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.toActivityDestination
import com.kieronquinn.app.taptap.utils.extensions.withStandardAnimations
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import net.dinglisch.android.tasker.TaskerIntent

class SettingsActionAddTaskerBottomSheetFragment: BaseBottomSheetDialogFragment() {

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?): MaterialDialog = materialDialog.apply {
        title(R.string.bs_tasker_title)
        message(R.string.bs_tasker_content)
        noAutoDismiss()
        positiveButton(R.string.bs_tasker_positive){
            runCatching {
                ActivityNavigator(requireContext()).navigate(Intent("net.dinglisch.android.tasker.ACTION_OPEN_PREFS").apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    //This seems to set the current tab. Misc is the 4th tab.
                    putExtra("tno", 3)
                }.toActivityDestination(requireContext()), null, NavOptions.Builder().withStandardAnimations().build(), null)
            }
        }
        negativeButton(android.R.string.cancel){
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        if(checkTaskerPermission(requireContext())){
            findNavController().navigateUp()
        }
    }

    private fun checkTaskerPermission(context: Context): Boolean {
        return TaskerIntent.testStatus(context) == TaskerIntent.Status.OK
    }

}