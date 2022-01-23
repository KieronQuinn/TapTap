package com.kieronquinn.app.taptap.ui.screens.setup.gesture

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.GestureConfigurationNavigation
import com.kieronquinn.app.taptap.components.navigation.RootNavigation
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.repositories.demomode.DemoModeRepository
import com.kieronquinn.app.taptap.repositories.phonespecs.PhoneSpecsRepository
import com.kieronquinn.app.taptap.service.foreground.TapTapForegroundService
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupViewModel
import com.kieronquinn.app.taptap.utils.extensions.instantCombine
import com.kieronquinn.monetcompat.core.MonetCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToLong
import kotlin.reflect.KFunction2

abstract class SetupGestureViewModel: BaseSetupViewModel() {

    abstract val state: StateFlow<State>

    abstract val tapEvents: Flow<Unit>
    abstract val doubleTapEvents: Flow<Unit>
    abstract val tripleTapEvents: Flow<Unit>

    abstract val bottomSheetCloseBus: Flow<Unit>
    abstract val bottomSheetOpenBus: Flow<Unit>
    abstract val bottomSheetRoundedCornerMultiplier: StateFlow<Float>
    abstract val bottomSheetStatusBarBlockHeight: StateFlow<Int>
    abstract val bottomSheetNavBarBlockHeight: StateFlow<Int>
    abstract val bottomSheetExpanded: StateFlow<Boolean>
    abstract val bottomSheetDraggable: StateFlow<Boolean>
    abstract val toolbarIconPlaytime: StateFlow<Long>
    abstract val toolbarHeightMultiplier: StateFlow<Float>
    abstract val toolbarIcon: StateFlow<ToolbarIcon>
    abstract val infoCard: StateFlow<InfoCard>

    abstract fun onConfigurationBackPressed()
    abstract fun startDemoMode(context: Context)
    abstract fun stopDemoMode(context: Context)
    abstract fun onInsetsChange(insets: WindowInsetsCompat)
    abstract fun onBottomSheetSlideOffsetChange(progress: Float)
    abstract fun setConfigurationBackAvailable(available: Boolean)
    abstract fun onNextClicked()

    sealed class State {
        object Loading: State()
        object Loaded: State()
    }

    enum class ToolbarIcon {
        CLOSE, CLOSE_TO_BACK, BACK_TO_CLOSE
    }

    enum class InfoCard(@DrawableRes val icon: Int, @StringRes val contentRes: Int, val cardColor: CardColor) {
        HINT(R.drawable.ic_about, R.string.setup_gesture_info_hint, CardColor.BACKGROUND_SECONDARY),
        HELP(R.drawable.ic_help, R.string.setup_gesture_info_help, CardColor.BACKGROUND_SECONDARY),
        SUCCESS(R.drawable.ic_check_circle, R.string.setup_gesture_info_success, CardColor.PRIMARY);

        enum class CardColor {
            BACKGROUND_SECONDARY, PRIMARY
        }
    }

}

class SetupGestureViewModelImpl(
    private val rootNavigation: RootNavigation,
    private val configurationNavigation: GestureConfigurationNavigation,
    private val demoModeRepository: DemoModeRepository,
    private val settings: TapTapSettings,
    private val phoneSpecsRepository: PhoneSpecsRepository
): SetupGestureViewModel() {

    companion object {
        private const val TOOLBAR_ICON_ANIMATION_LENGTH = 500L // 500ms
        private const val INFO_CARD_DWELL_TIME = 10000L // 10s
    }

    override val state = flow {
        //Await internet permission if required
        settings.internetAllowed.asFlowNullable().filterNotNull().take(1).collect()
        //Get and set a personalised default if it's not set and is possible
        if(settings.columbusTapModel.getOrNull() == null){
            getBestModel()?.let {
                settings.columbusTapModel.set(it)
            }
        }
        emit(State.Loaded)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val tapEvents = demoModeRepository.tapBus
    override val doubleTapEvents = demoModeRepository.doubleTapBus
    override val tripleTapEvents = demoModeRepository.tripleTapBus

    private val hasTapEventHappened = instantCombine(doubleTapEvents, tripleTapEvents).map {
        it.any { tap -> tap != null }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val currentInfoCard = flow {
        //Await loaded
        state.takeWhile { it is State.Loading }.collect()
        //Start looping between Hint & Help
        while(true) {
            emit(InfoCard.HINT)
            delay(INFO_CARD_DWELL_TIME)
            emit(InfoCard.HELP)
            delay(INFO_CARD_DWELL_TIME)
        }
    }

    override val infoCard = combine(hasTapEventHappened, currentInfoCard) { tapHappened, currentCard ->
        if(tapHappened){
            InfoCard.SUCCESS
        } else {
            currentCard
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, InfoCard.HINT)

    private val bottomSheetOffsetProgress = MutableStateFlow(0f)
    private val bottomInset = MutableStateFlow(0)
    private val topInset = MutableStateFlow(0)
    private val configurationBackAvailable = MutableStateFlow(false)

    override val bottomSheetCloseBus = MutableSharedFlow<Unit>()
    override val bottomSheetOpenBus = MutableSharedFlow<Unit>()

    override val bottomSheetRoundedCornerMultiplier = bottomSheetOffsetProgress.map { progress ->
        (1f - (progress * 2f).coerceAtMost(1f))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 1f)

    override val bottomSheetNavBarBlockHeight = combine(bottomInset, bottomSheetOffsetProgress) { inset, progress ->
        (inset * (1f - (progress * 2f).coerceAtMost(1f))).toInt()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    override val bottomSheetStatusBarBlockHeight = combine(topInset, bottomSheetOffsetProgress) { inset, progress ->
        (inset * ((progress - 0.5f).coerceAtLeast(0f) * 2f).coerceAtMost(1f)).toInt()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    override val bottomSheetExpanded = bottomSheetOffsetProgress.map {
        it > 0f
    }.debounce(250L).stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override val toolbarIconPlaytime = bottomSheetOffsetProgress.map {
        (it * TOOLBAR_ICON_ANIMATION_LENGTH).roundToLong()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    override val toolbarHeightMultiplier = bottomSheetOffsetProgress.map {
        1f - it
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 1f)

    override val bottomSheetDraggable = configurationBackAvailable.map {
        !it
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private var currentToolbarIcon = ToolbarIcon.CLOSE

    override val toolbarIcon = combine(bottomSheetOffsetProgress, configurationBackAvailable) { progress, backAvailable ->
        when {
            progress < 1f -> ToolbarIcon.CLOSE
            backAvailable -> ToolbarIcon.CLOSE_TO_BACK
            currentToolbarIcon == ToolbarIcon.CLOSE_TO_BACK -> ToolbarIcon.BACK_TO_CLOSE
            else -> ToolbarIcon.CLOSE
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ToolbarIcon.CLOSE).apply {
        viewModelScope.launch {
            collect {
                currentToolbarIcon = it
            }
        }
    }

    override fun startDemoMode(context: Context) {
        viewModelScope.launch {
            demoModeRepository.setDemoModeEnabled(true)
            startService(context, false)
        }
    }

    override fun stopDemoMode(context: Context) {
        //Fragment might be going so break out of scope
        GlobalScope.launch {
            demoModeRepository.setDemoModeEnabled(false)
            stopService(context)
            if(settings.serviceEnabled.getSync()) {
                startService(context, true)
            }
        }
    }

    override fun onConfigurationBackPressed() {
        viewModelScope.launch {
            when {
                configurationBackAvailable.value -> {
                    configurationNavigation.navigateBack()
                }
                bottomSheetExpanded.value -> {
                    bottomSheetCloseBus.emit(Unit)
                }
                else -> {
                    bottomSheetOpenBus.emit(Unit)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        viewModelScope.launch {
            when {
                configurationBackAvailable.value -> {
                    configurationNavigation.navigateBack()
                }
                bottomSheetExpanded.value -> {
                    bottomSheetCloseBus.emit(Unit)
                }
                else -> {
                    rootNavigation.navigateBack()
                }
            }
        }
        return true
    }

    override fun onInsetsChange(insets: WindowInsetsCompat) {
        viewModelScope.launch {
            bottomInset.emit(insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            topInset.emit(insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
        }
    }

    override fun onBottomSheetSlideOffsetChange(progress: Float) {
        viewModelScope.launch {
            bottomSheetOffsetProgress.emit(progress)
        }
    }

    override fun setConfigurationBackAvailable(available: Boolean) {
        viewModelScope.launch {
            configurationBackAvailable.emit(available)
        }
    }

    override fun onNextClicked() {
        viewModelScope.launch {
            if(!settings.serviceEnabled.exists()){
                //Enable the service on continuing if it's not been disabled manually
                settings.serviceEnabled.set(true)
            }
            rootNavigation.navigate(SetupGestureFragmentDirections.actionSetupGestureFragmentToSetupCompleteFragment())
        }
    }

    /**
     *  Returns the best **Newest** model for this device's specs. Will return null if device
     *  not found or internet is disabled.
     */
    private suspend fun getBestModel(): TapModel? {
        val specs = phoneSpecsRepository.getDeviceSpecs() ?: return null
        return phoneSpecsRepository.getBestModels(specs).first
    }

    /**
     *  Start calls are locked for 2.5 seconds after launch (theoretically should be 5s, but 2.5s
     *  seems to be enough 99% of the time), to prevent ForegroundServiceDidNotStartInTimeException
     *  from calling stopService before startForegroundService
     */

    private val serviceLock = Mutex()

    private suspend fun startService(context: Context, isRestart: Boolean) = serviceLock.withLock {
        withContext(Dispatchers.IO) {
            TapTapForegroundService.start(context, isRestart)
            delay(2500L)
        }
    }

    private suspend fun stopService(context: Context) = serviceLock.withLock {
        TapTapForegroundService.stop(context)
    }

}