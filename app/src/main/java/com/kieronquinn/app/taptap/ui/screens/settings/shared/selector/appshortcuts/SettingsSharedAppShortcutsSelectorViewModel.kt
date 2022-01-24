package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.appshortcuts

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ParceledListSlice
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutQueryWrapper
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.ParcelFileDescriptor
import android.util.Base64
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.models.appshortcut.AppShortcutCachedIcon
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository.ShizukuServiceResponse
import com.kieronquinn.app.taptap.repositories.service.TapTapShizukuServiceRepository.ShizukuServiceResponse.Failed.Reason
import com.kieronquinn.app.taptap.utils.extensions.getApplicationLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.createScope
import org.koin.core.component.inject
import java.io.File

abstract class SettingsSharedAppShortcutsSelectorViewModel: ViewModel(), KoinScopeComponent, KoinComponent {

    abstract val state: StateFlow<State>
    abstract fun onAppClicked(packageName: String): List<Int>
    abstract fun onAppShortcutClicked()

    sealed class State {
        object Loading: State()
        data class Loaded(val items: List<Item>): State()
        data class Error(val reason: Reason): State()
    }

    sealed class Item(val itemType: ItemType) {
        data class App(val name: CharSequence, val packageName: String, var isOpen: Boolean = false): Item(ItemType.APP)
        data class AppShortcut(val icon: AppShortcutCachedIcon, val name: CharSequence, val packageName: String, val shortcutId: String, var isVisible: Boolean = false): Item(ItemType.APP_SHORTCUT)

        enum class ItemType {
            APP, APP_SHORTCUT
        }
    }

}

@Suppress("newapi")
class SettingsSharedAppShortcutsSelectorViewModelImpl(context: Context, private val navigation: ContainerNavigation): SettingsSharedAppShortcutsSelectorViewModel() {

    private val packageManager = context.packageManager

    private val cacheDir by lazy {
        File(context.cacheDir, "appshortcuts").also {
            it.mkdirs()
        }
    }

    override val scope by lazy {
        createScope(this)
    }

    override fun onCleared() {
        super.onCleared()
        cacheDir.deleteRecursively()
        scope.close()
    }

    private val service by inject<TapTapShizukuServiceRepository>()

    override val state = MutableStateFlow<State>(State.Loading).apply {
        viewModelScope.launch {
            emit(loadState(context))
        }
    }

    private suspend fun loadState(context: Context): State = withContext(Dispatchers.IO) {
        return@withContext when (val shortcuts = getAppShortcuts()) {
            is ShizukuServiceResponse.Success -> {
                when(val shortcutItems = shortcuts.result.list.toShortcutItems(context)){
                    is ShizukuServiceResponse.Success -> State.Loaded(shortcutItems.result)
                    is ShizukuServiceResponse.Failed -> State.Error(shortcutItems.reason)
                }
            }
            is ShizukuServiceResponse.Failed -> State.Error(shortcuts.reason)
        }
    }

    @SuppressLint("NewApi")
    private suspend fun getAppShortcuts() = try {
        service.runWithService {
            it.getShortcuts(ShortcutQueryWrapper(LauncherApps.ShortcutQuery().apply {
                setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED_BY_ANY_LAUNCHER)
            })) as ParceledListSlice<ShortcutInfo>
        }
    }catch(e: Exception){
        ShizukuServiceResponse.Failed(Reason.Custom(R.string.settings_shared_shortcuts_selector_error))
    }

    private suspend fun List<ShortcutInfo>.toShortcutItems(context: Context) = service.runWithService { service ->
        val splitList = ArrayList<Pair<Item.App, List<Item.AppShortcut>>>()
        val grouped = groupBy { it.`package` }
        grouped.forEach { (a, s) ->
            val app = Item.App(packageManager.getApplicationLabel(a) ?: "", a)
            val shortcuts = s.map {
                val icon = service.getAppShortcutIcon(a, it.id)
                val hashedShortcutId = Base64.encodeToString(it.id.toByteArray(), Base64.DEFAULT)
                val cacheIcon = if(icon.descriptor != null){
                    copyFileDescriptorToCache(icon.descriptor, hashedShortcutId)
                }else copyIconToCache(icon.icon, context, hashedShortcutId)
                Item.AppShortcut(AppShortcutCachedIcon(icon.icon, cacheIcon), it.shortLabel ?: it.longLabel ?: it.id, it.`package`, it.id)
            }.sortedBy { it.name.toString().lowercase() }
            splitList.add(Pair(app, shortcuts))
        }
        splitList.sortBy { it.first.name.toString().lowercase() }
        val outList = ArrayList<Item>()
        splitList.forEach {
            outList.add(it.first)
            outList.addAll(it.second)
        }
        return@runWithService outList
    }

    override fun onAppClicked(packageName: String): List<Int> {
        (state.value as? State.Loaded)?.items?.let { items ->
            val indexes = ArrayList<Int>()
            items.filterIndexed { index, item ->
                (item is Item.AppShortcut && item.packageName == packageName).also {
                    if (it) indexes.add(index)
                }
            }.forEach {
                (it as? Item.AppShortcut)?.let { item ->
                    item.isVisible = !item.isVisible
                }
            }
            return indexes
        }
        return emptyList()
    }

    override fun onAppShortcutClicked() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_shared_picker_app_shortcut, true)
        }
    }

    private fun copyFileDescriptorToCache(descriptor: ParcelFileDescriptor, shortcutId: String): File {
        val file = File(cacheDir, "$shortcutId.png")
        file.outputStream().use {
            ParcelFileDescriptor.AutoCloseInputStream(descriptor).copyTo(it)
        }
        return file
    }

    private fun copyIconToCache(icon: Icon?, context: Context, shortcutId: String): File? {
        if(icon == null) return null
        val file = File(cacheDir, "$shortcutId.png")
        val bitmap = icon.loadDrawable(context)?.toBitmap() ?: return null
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
        return file
    }

}