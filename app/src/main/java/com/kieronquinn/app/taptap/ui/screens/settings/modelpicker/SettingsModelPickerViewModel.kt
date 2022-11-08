package com.kieronquinn.app.taptap.ui.screens.settings.modelpicker

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.models.phonespecs.DeviceSpecs
import com.kieronquinn.app.taptap.repositories.phonespecs.PhoneSpecsRepository
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class SettingsModelPickerViewModel : GenericSettingsViewModel() {

    abstract val state: StateFlow<State>

    abstract fun onTabSelected(index: Int)
    abstract fun onModelSelected(model: TapModel)

    sealed class Item(val itemType: ItemType) {
        data class Header(@StringRes val contentRes: Int): Item(ItemType.HEADER)
        data class Model(val model: TapModel, var selected: Boolean, val best: Boolean): Item(ItemType.MODEL)

        enum class ItemType {
            HEADER, MODEL
        }
    }

    sealed class State {
        object Loading : State()
        data class Loaded(
            val deviceSpecs: DeviceSpecs?,
            val items: List<Item>,
            val selectedTab: Int?
        ): State()
    }

}

class SettingsModelPickerViewModelImpl(phoneSpecsRepository: PhoneSpecsRepository, settings: TapTapSettings): SettingsModelPickerViewModel() {

    private val phoneSpecs = flow {
        emit(phoneSpecsRepository.getDeviceSpecs())
    }

    private val newestModels = flow {
        emit(TapModel.values().filter { it.modelType == TapModel.ModelType.NEW })
    }

    private val legacyModels = flow {
        emit(TapModel.values().filter { it.modelType == TapModel.ModelType.LEGACY })
    }

    private val oemModels = flow {
        emit(TapModel.values().filter { it.modelType == TapModel.ModelType.OEM })
    }

    private val newestHeader by lazy {
        Item.Header(R.string.settings_model_picker_header_newest)
    }

    private val legacyHeader by lazy {
        Item.Header(R.string.settings_model_picker_header_legacy)
    }

    private val oemHeader by lazy {
        Item.Header(R.string.settings_model_picker_header_oem)
    }

    private val selectedModel = settings.columbusTapModel
    private val customSensitivity = settings.columbusCustomSensitivity
    private val selectedModelFlow = selectedModel.asFlow()

    private val selectedTab = MutableStateFlow<Int?>(null)

    override val restartService = restartServiceCombine(selectedModelFlow.map {  })

    private val loadedModels = combine(newestModels, legacyModels, oemModels) { newest, legacy, oem ->
        listOf(newest, legacy, oem)
    }

    private val models = combine(loadedModels, selectedTab, selectedModelFlow, phoneSpecs) { allModels, selectedTab, selectedModel, specs ->
        val newest = allModels[0]
        val legacy = allModels[1]
        val oem = allModels[2]
        val bestModelsPair = specs?.let {
            phoneSpecsRepository.getBestModels(it)
        }
        val tabIndex =
            //User has selected a tab, use that
            selectedTab ?: //User has not selected a tab, show the tab containing their chosen model
                when {
                    newest.contains(selectedModel) -> 0
                    legacy.contains(selectedModel) -> 1
                    oem.contains(selectedModel) -> 2
                    else -> 0 //Default to first tab
                }
        val bestModel = when(tabIndex) {
            0 -> bestModelsPair?.first
            1 -> bestModelsPair?.second
            2 -> null //No predictions for OEM
            else -> throw RuntimeException("Invalid tab")
        }
        val models = when(tabIndex) {
            0 -> newest
            1 -> legacy
            2 -> oem
            else -> throw RuntimeException("Invalid tab")
        }
        Triple(tabIndex, models, bestModel)
    }

    override val state = combine(phoneSpecs, selectedModelFlow, models, selectedTab) { specs, selectedModel, allModels, selectedTabIndex ->
        val uiModels = allModels.second.map {
            Item.Model(it, selectedModel == it, allModels.third == it)
        }
        val items = when(allModels.first){
            0 -> listOf(newestHeader) + uiModels
            1 -> listOf(legacyHeader) + uiModels
            2 -> listOf(oemHeader) + uiModels
            else -> throw RuntimeException("Invalid tab")
        }
        State.Loaded(
            specs,
            items,
            allModels.first
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onTabSelected(index: Int) {
        viewModelScope.launch {
            selectedTab.emit(index)
        }
    }

    override fun onModelSelected(model: TapModel) {
        viewModelScope.launch {
            val oldModel = selectedModel.get()
            //Clear the custom sensitivity if it is set, if the model is changing drastically
            if(oldModel.modelType != model.modelType){
                customSensitivity.clear()
            }
            selectedModel.set(model)
        }
    }

}