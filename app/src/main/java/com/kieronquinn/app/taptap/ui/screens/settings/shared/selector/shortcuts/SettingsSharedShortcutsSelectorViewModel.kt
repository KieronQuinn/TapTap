package com.kieronquinn.app.taptap.ui.screens.settings.shared.selector.shortcuts

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.utils.extensions.getApplicationLabel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SettingsSharedShortcutsSelectorViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract fun onAppClicked(packageName: String): List<Int>
    abstract fun unwind()

    sealed class State {
        object Loading: State()
        data class Loaded(val items: List<Item>): State()
    }

    sealed class Item(val itemType: ItemType) {
        data class App(val name: CharSequence, val packageName: String, var isOpen: Boolean = false): Item(ItemType.APP)
        data class Shortcut(val name: CharSequence, val packageName: String, val activity: String, var isVisible: Boolean = false): Item(ItemType.SHORTCUT)

        enum class ItemType {
            APP, SHORTCUT
        }
    }

}

class SettingsSharedShortcutsSelectorViewModelImpl(context: Context, private val navigation: ContainerNavigation): SettingsSharedShortcutsSelectorViewModel() {

    private val packageManager = context.packageManager

    override val state = flow {
        val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
        val resolveInfos = packageManager.queryIntentActivities(shortcutIntent, 0)
        val groupedShortcuts = resolveInfos.groupBy { it.activityInfo.packageName }
        val splitList = ArrayList<Pair<Item.App, List<Item.Shortcut>>>()
        groupedShortcuts.forEach { (packageName, shortcuts) ->
            val app = Item.App(packageManager.getApplicationLabel(packageName) ?: "", packageName)
            val shortcutItems = shortcuts.map {
                Item.Shortcut(it.loadLabel(packageManager), it.activityInfo.packageName, it.activityInfo.name)
            }.sortedBy { it.name.toString().lowercase() }
            splitList.add(Pair(app, shortcutItems))
        }
        splitList.sortBy { it.first.name.toString().lowercase() }
        val outList = ArrayList<Item>()
        splitList.forEach {
            outList.add(it.first)
            outList.addAll(it.second)
        }
        emit(State.Loaded(outList))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onAppClicked(packageName: String): List<Int> {
        (state.value as? State.Loaded)?.items?.let { items ->
            val indexes = ArrayList<Int>()
            items.filterIndexed { index, item ->
                (item is Item.Shortcut && item.packageName == packageName).also {
                    if (it) indexes.add(index)
                }
            }.forEach {
                (it as? Item.Shortcut)?.let { item ->
                    item.isVisible = !item.isVisible
                }
            }
            return indexes
        }
        return emptyList()
    }

    override fun unwind() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_shared_picker_shortcut, true)
        }
    }

}