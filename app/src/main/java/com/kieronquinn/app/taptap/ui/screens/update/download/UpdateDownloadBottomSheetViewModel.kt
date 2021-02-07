package com.kieronquinn.app.taptap.ui.screens.update.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

class UpdateDownloadBottomSheetViewModel(private val downloadManager: DownloadManager): ViewModel() {

    private var requestId: Long? = null
    private var _downloadState = MutableStateFlow<State>(State.Idle)
    private var downloadFile: File? = null

    val downloadState = _downloadState.asStateFlow()

    fun startDownload(fragment: BottomSheetDialogFragment, url: String, fileName: String) {
        UpdateDownloadBottomSheetFragment().show(fragment.parentFragmentManager, "bs_update_download")
        downloadUpdate(fragment.requireContext(), url, fileName)
        fragment.dismiss()
    }

    private val downloadStateReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            viewModelScope.launch {
                var success = false
                val query = DownloadManager.Query().apply {
                    setFilterById(requestId ?: return@apply)
                }
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                        success = true
                    }
                }
                if (success && downloadFile != null) {
                    val outputUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", downloadFile!!)
                    _downloadState.emit(State.Done(outputUri))
                } else {
                    _downloadState.emit(State.Failed)
                }
            }
        }
    }

    fun openPackageInstaller(context: Context, uri: Uri){
        Intent(Intent.ACTION_VIEW, uri).apply {
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.also {
            context.startActivity(it)
        }
    }

    private val downloadObserver = object: ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            viewModelScope.launch {
                val query = DownloadManager.Query()
                query.setFilterById(requestId ?: return@launch)
                val c: Cursor = downloadManager.query(query)
                var progress = 0.0
                if (c.moveToFirst()) {
                    val sizeIndex: Int =
                        c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val downloadedIndex: Int =
                        c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val size = c.getInt(sizeIndex)
                    val downloaded = c.getInt(downloadedIndex)
                    if (size != -1) progress = downloaded * 100.0 / size
                }
                _downloadState.emit(State.Downloading(progress.roundToInt()))
            }
        }
    }

    private fun downloadUpdate(context: Context, url: String, fileName: String) = viewModelScope.launch {
        val downloadFolder = File(context.externalCacheDir, "updates").apply {
            mkdirs()
        }
        downloadFile = File(downloadFolder, fileName)
        context.registerReceiver(downloadStateReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        context.contentResolver.registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, downloadObserver)
        requestId = DownloadManager.Request(Uri.parse(url)).apply {
            setDescription(context.getString(R.string.download_manager_description))
            setTitle(context.getString(R.string.app_name))
            setDestinationUri(Uri.fromFile(downloadFile!!))
        }.run {
            downloadManager.enqueue(this)
        }
    }

    fun cancelDownload(fragment: BottomSheetDialogFragment) = viewModelScope.launch {
        requestId?.let {
            downloadManager.remove(it)
        }
        fragment.requireContext().unregisterReceiver(downloadStateReceiver)
        fragment.requireContext().contentResolver.unregisterContentObserver(downloadObserver)
        _downloadState.emit(State.Idle)
        fragment.dismiss()
    }

    sealed class State {
        object Idle: State()
        data class Downloading(val progress: Int): State()
        data class Done(val fileUri: Uri): State()
        object Failed: State()
    }

}