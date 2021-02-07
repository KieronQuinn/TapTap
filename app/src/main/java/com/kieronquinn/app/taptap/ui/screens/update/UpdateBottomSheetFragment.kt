package com.kieronquinn.app.taptap.ui.screens.update

import android.os.Bundle
import android.text.format.DateFormat
import com.afollestad.materialdialogs.MaterialDialog
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.UpdateChecker
import com.kieronquinn.app.taptap.utils.extensions.getSpannedText
import com.kieronquinn.app.taptap.utils.launchCCT
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.update.download.UpdateDownloadBottomSheetViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.time.Instant
import java.util.*

class UpdateBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val updateViewModel by sharedViewModel<UpdateDownloadBottomSheetViewModel>()
    private val updateChecker by inject<UpdateChecker>()

    companion object {
        const val KEY_UPDATE = "update"
    }

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?) = materialDialog.apply {
        title(R.string.bs_update_title)
        val update = requireArguments().getParcelable<UpdateChecker.Update>(KEY_UPDATE)!!
        val timestamp = DateFormat.getDateFormat(view.context).format(Date.from(Instant.parse(update.timestamp)))
        val message = getSpannedText(
            getString(
                R.string.bs_update_content,
                BuildConfig.VERSION_NAME,
                update.name,
                timestamp,
                update.changelog.formatChangelog()
            )
        )
        message(text = message)
        noAutoDismiss()
        positiveButton(R.string.bs_update_download){
            updateChecker.hasDismissedDialog = true
            if(update.assetUrl.endsWith(".apk")) {
                updateViewModel.startDownload(this@UpdateBottomSheetFragment, update.assetUrl, update.assetName)
            }else{
                requireContext().launchCCT(update.releaseUrl)
            }
        }
        negativeButton(R.string.configuration_button_close){
            updateChecker.hasDismissedDialog = true
            dismiss()
        }
        neutralButton(R.string.bs_update_download_github){
            requireContext().launchCCT(update.releaseUrl)
            updateChecker.hasDismissedDialog = true
            dismiss()
        }
    }

    private fun String.formatChangelog(): String {
        return this.replace("\n", "<br>")
    }

}