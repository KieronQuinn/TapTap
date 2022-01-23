package com.kieronquinn.app.taptap.ui.screens.settings.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.update.Release
import com.kieronquinn.app.taptap.utils.extensions.broadcastReceiverAsFlow
import com.kieronquinn.app.taptap.utils.extensions.observerAsFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

abstract class SettingsUpdateViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract val showFab: StateFlow<Boolean>

    abstract fun setupWithRelease(release: Release)
    abstract fun onDownloadBrowserClicked(url: String)
    abstract fun startDownload()
    abstract fun startInstall()
    abstract fun onPause()
    abstract fun onResume()

    sealed class State {
        object Loading: State()
        data class Info(val release: Release): State()
        data class StartDownload(val url: String, val fileName: String): State()
        data class Downloading(val requestId: Long, val progress: Double, val downloadFile: File): State()
        data class StartInstall(val outputUri: Uri): State()
        data class Done(val outputUri: Uri): State()
        object Failed: State()
    }

}

class SettingsUpdateViewModelImpl(private val navigation: ContainerNavigation, context: Context): SettingsUpdateViewModel() {

    private val downloadManagerTitle = context.getString(R.string.app_name)
    private val downloadManagerDescription = context.getString(R.string.download_manager_description)
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val downloadFolder = File(context.externalCacheDir, "updates").apply {
        mkdirs()
    }
    private val downloadObserver = context.contentResolver.observerAsFlow(Uri.parse("content://downloads/my_downloads"), true)
    private val downloadComplete = context.broadcastReceiverAsFlow(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    private val getUriForFile = { downloadFile: File ->
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", downloadFile)
    }
    private val resumed = MutableStateFlow(false)

    override val state = MutableStateFlow<State>(State.Loading)
    override val showFab = combine(resumed, state){ r, s ->
        r && s is State.Info
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private fun setupDownloadObserver() = viewModelScope.launch {
        downloadObserver.collect {
            handleDownloadUpdate()
        }
    }

    private suspend fun handleDownloadUpdate() {
        val downloading = state.value as? State.Downloading ?: return
        val query = DownloadManager.Query()
        query.setFilterById(downloading.requestId)
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
        state.emit(State.Downloading(downloading.requestId, progress, downloading.downloadFile))
    }

    private fun setupDownloadCompleteListener() = viewModelScope.launch {
        downloadComplete.collect {
            handleDownloadComplete()
        }
    }

    private suspend fun handleDownloadComplete() = viewModelScope.launch {
        val downloading = state.value as? State.Downloading
        val requestId = downloading?.requestId ?: return@launch
        val downloadFile = downloading.downloadFile
        var success = false
        val query = DownloadManager.Query().apply {
            setFilterById(requestId)
        }
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (cursor.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                success = true
            }
        }
        if (success) {
            val outputUri = getUriForFile(downloadFile)
            state.emit(State.StartInstall(outputUri))
        } else {
            state.emit(State.Failed)
        }
    }

    private fun setupDownloadStartEndListener() = viewModelScope.launch {
        state.collect {
            when (it) {
                is State.StartDownload -> {
                    onStartDownload(it.url, it.fileName)
                }
                is State.StartInstall -> {
                    state.emit(State.Done(it.outputUri))
                    startInstall()
                }
            }
        }
    }

    override fun setupWithRelease(release: Release) {
        viewModelScope.launch {
            if(state.value is State.Loading){
                state.emit(State.Info(release))
            }
        }
    }

    override fun onDownloadBrowserClicked(url: String) {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        }
    }

    override fun startDownload() {
        val release = (state.value as? State.Info)?.release ?: return
        viewModelScope.launch {
            state.emit(State.StartDownload(release.downloadUrl, release.fileName))
        }
    }

    override fun startInstall() {
        val uri = (state.value as? State.Done)?.outputUri ?: (state.value as? State.StartInstall)?.outputUri ?: return
        viewModelScope.launch {
            Intent(Intent.ACTION_VIEW, uri).apply {
                putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.also {
                navigation.navigate(it)
            }
        }
    }

    private suspend fun onStartDownload(url: String, fileName: String){
        val downloadFile = File(downloadFolder, fileName)
        val requestId = downloadManager.enqueue(DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(downloadManagerTitle)
            setDescription(downloadManagerDescription)
            setDestinationUri(Uri.fromFile(downloadFile))
        })
        state.emit(State.Downloading(requestId, 0.0, downloadFile))
    }

    override fun onPause() {
        viewModelScope.launch {
            resumed.emit(false)
        }
    }

    override fun onResume() {
        viewModelScope.launch {
            resumed.emit(true)
        }
    }

    init {
        setupDownloadCompleteListener()
        setupDownloadObserver()
        setupDownloadStartEndListener()
    }

}