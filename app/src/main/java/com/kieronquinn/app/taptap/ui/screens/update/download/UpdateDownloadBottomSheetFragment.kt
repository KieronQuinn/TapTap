package com.kieronquinn.app.taptap.ui.screens.update.download

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentUpdateDownloadBottomSheetBinding
import com.kieronquinn.app.taptap.components.base.BaseBottomSheetDialogFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.sharedViewModel

class UpdateDownloadBottomSheetFragment: BaseBottomSheetDialogFragment() {

    private val updateViewModel by sharedViewModel<UpdateDownloadBottomSheetViewModel>()
    private lateinit var binding: FragmentUpdateDownloadBottomSheetBinding

    override fun onMaterialDialogCreated(materialDialog: MaterialDialog, savedInstanceState: Bundle?) = materialDialog.apply {
        title(R.string.bs_update_download_title)
        binding = FragmentUpdateDownloadBottomSheetBinding.inflate(layoutInflater)
        customView(view = binding.root)
        negativeButton(R.string.restore_prompt_cancel){
            updateViewModel.cancelDownload(this@UpdateDownloadBottomSheetFragment)
        }
        noAutoDismiss()
        cancelOnTouchOutside(false)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            updateViewModel.downloadState.collect {
                when(it){
                    is UpdateDownloadBottomSheetViewModel.State.Downloading -> {
                        if(it.progress > 0) {
                            binding.fragmentUpdateDownloadProgress.isIndeterminate = false
                            binding.fragmentUpdateDownloadProgress.progress = it.progress
                        }
                    }
                    is UpdateDownloadBottomSheetViewModel.State.Done -> {
                        updateViewModel.openPackageInstaller(requireContext(), it.fileUri)
                        dismiss()
                    }
                    is UpdateDownloadBottomSheetViewModel.State.Failed -> {
                        Toast.makeText(requireContext(), R.string.bs_update_download_failed, Toast.LENGTH_LONG).show()
                        dismiss()
                    }
                }
            }
        }
    }

}