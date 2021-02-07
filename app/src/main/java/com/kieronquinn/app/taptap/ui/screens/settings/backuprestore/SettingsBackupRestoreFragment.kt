package com.kieronquinn.app.taptap.ui.screens.settings.backuprestore

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.airbnb.lottie.LottieDrawable
import com.kieronquinn.app.taptap.databinding.FragmentBackupRestoreBinding
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.settings.backuprestore.restore.SettingsBackupRestoreRestoreViewModel
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsBackupRestoreFragment: BoundFragment<FragmentBackupRestoreBinding>(FragmentBackupRestoreBinding::class.java) {

    override val disableToolbarBackground = true

    private val viewModel by sharedViewModel<SettingsBackupRestoreViewModel>()
    private val restoreViewModel by sharedViewModel<SettingsBackupRestoreRestoreViewModel>()

    val backupPickerResult = registerForActivityResult(ActivityResultContracts.CreateDocument()){
        it?.let {
            viewModel.onBackupLocationPicked(this, it)
        }
    }

    val restorePickerResult = registerForActivityResult(ActivityResultContracts.OpenDocument()){
        it?.let {
            viewModel.onRestoreFilePicked(this, it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            root.applySystemWindowInsetsToPadding(top = true, bottom = true)
            fragmentBackupRestoreLottie.run {
                clipToOutline = true
                repeatCount = LottieDrawable.INFINITE
                speed = 0.5f
                playAnimation()
            }

        }
        bindToNothing()
    }

    override fun onResume() {
        super.onResume()
        restoreViewModel.reset()
    }

    override fun onBackPressed() = viewModel.onBackPressed(this)

}