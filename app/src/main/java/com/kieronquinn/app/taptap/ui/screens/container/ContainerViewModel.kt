package com.kieronquinn.app.taptap.ui.screens.container

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.WindowInsets
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import com.kieronquinn.app.taptap.utils.UpdateChecker
import com.kieronquinn.app.taptap.utils.extensions.update
import com.kieronquinn.app.taptap.components.base.BaseViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.main.beta.SettingsMainBetaBottomSheetDialogFragment
import com.kieronquinn.app.taptap.ui.screens.update.UpdateBottomSheetFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class ContainerViewModel(private val tapSharedPreferences: TapSharedPreferences): BaseViewModel() {

    private val flowSharedPreferences = tapSharedPreferences.flowSharedPreferences

    private val splashState = MutableLiveData<SplashState>()

    private val hasSeenSetup = flowSharedPreferences.getBoolean(TapSharedPreferences.SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP).asFlow().asLiveData()

    private val gestureEnabled = flowSharedPreferences.getBoolean(TapSharedPreferences.SHARED_PREFERENCES_KEY_MAIN_SWITCH, true).asFlow().asLiveData()

    private val tripleTapEnabled = flowSharedPreferences.getBoolean(TapSharedPreferences.SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH).asFlow().asLiveData()

    private val currentDestination = MutableLiveData<Int>()

    val containerState = MediatorLiveData<ContainerState>().apply {
        val update = {
            when {
                hasSeenSetup.value == true -> {
                    //Don't show splash if setup is already done
                    update(ContainerState.Settings)
                }
                splashState.value == SplashState.DONE -> {
                    update(ContainerState.Setup)
                }
                else -> {
                    update(ContainerState.Splash(SplashState.SHOWING))
                }
            }
        }
        addSource(hasSeenSetup) {
            update()
        }
        addSource(splashState) {
            update()
        }
    }

    private val containerDepth = MutableLiveData<Int>()

    val shouldShowBack = MediatorLiveData<Boolean>().apply {
        addSource(containerDepth){
            update(it > 0)
        }
    }

    val shouldShowToolbar = MediatorLiveData<Boolean>().apply {
        addSource(containerState){
            update(when(it){
                is ContainerState.Settings -> true
                else -> false
            })
        }
    }

    val shouldShowMenu = MediatorLiveData<Boolean>().apply {
        addSource(currentDestination){
            update(it == R.id.settingsFragment)
        }
    }

    val switchState = MediatorLiveData<SwitchState>().apply {
        addSource(currentDestination){
            update(when(it){
                R.id.settingsFragment -> SwitchState.MAIN
                R.id.settingsActionTripleFragment -> SwitchState.TRIPLE
                else -> SwitchState.HIDDEN
            })
        }
        update(SwitchState.HIDDEN)
    }

    val switchCheckedState = MediatorLiveData<Boolean>().apply {
        val update = Observer<Any> {
            when(switchState.value){
                SwitchState.MAIN -> update(gestureEnabled.value)
                SwitchState.TRIPLE -> update(tripleTapEnabled.value)
                else -> {} //Do nothing
            }
        }
        addSource(switchState, update)
        addSource(gestureEnabled, update)
        addSource(tripleTapEnabled, update)
    }

    val toolbarTitle = MutableLiveData<CharSequence?>()

    private val statusBarSize = MutableLiveData<Int>()
    val navigationBarSize = MutableLiveData<Int>()

    val topInsetChange = MediatorLiveData<Unit>().apply {
        val update = Observer<Any> { postValue(Unit) }
        addSource(statusBarSize, update)
        addSource(shouldShowToolbar, update)
        addSource(switchState, update)
        addSource(containerDepth, update)
    }

    fun getTopInset(context: Context): Int {
        val actionBarHeight = TypedValue().run {
            if(context.theme.resolveAttribute(R.attr.actionBarSize, this, true)){
                TypedValue.complexToDimensionPixelSize(data, context.resources.displayMetrics)
            }else 0
        }
        val toolbarHeight = if(shouldShowToolbar.value == true) actionBarHeight else 0
        val switchHeight = if(switchState.value != SwitchState.HIDDEN) actionBarHeight else 0
        return (statusBarSize.value ?: 0) + toolbarHeight + switchHeight
    }

    private val scrollOffset = MutableLiveData<Int>()
    val shouldShowToolbarShadow = MediatorLiveData<Boolean>().apply {
        addSource(scrollOffset){
            update(it > 0)
        }
    }

    val shouldDisableToolbarBackground = MutableLiveData(false)

    fun updateScrollOffset(position: Int){
        scrollOffset.update(position)
    }

    fun notifyInsetsChanged(insets: WindowInsets) {
        statusBarSize.update(insets.systemWindowInsetTop)
        navigationBarSize.update(insets.systemWindowInsetBottom)
    }

    fun notifySplashFinished(){
        splashState.update(SplashState.DONE)
    }

    fun notifyContainerFragmentDepthChanged(depth: Int){
        containerDepth.update(depth)
    }

    fun onSwitchClicked(){
        when(switchState.value ?: SwitchState.HIDDEN){
            SwitchState.MAIN -> tapSharedPreferences.isMainEnabled = !tapSharedPreferences.isMainEnabled
            SwitchState.TRIPLE -> tapSharedPreferences.isTripleTapEnabled = !tapSharedPreferences.isTripleTapEnabled
            else -> {} //Do nothing
        }
    }

    fun setCurrentDestination(@IdRes currentDestination: Int){
        this.currentDestination.update(currentDestination)
    }

    @StringRes
    fun getSwitchText(): Int? {
        return when(switchState.value ?: SwitchState.HIDDEN){
            SwitchState.HIDDEN -> null
            SwitchState.MAIN -> R.string.switch_main
            SwitchState.TRIPLE -> R.string.switch_triple
        }
    }

    fun onMenuItemSelected(menuItem: MenuItem, fragment: Fragment){
        when(menuItem.itemId){
            R.id.menu_setup_wizard -> tapSharedPreferences.hasSeenSetup = false
            R.id.menu_beta -> showBetaBottomSheet(fragment)
            R.id.menu_update -> showUpdateBottomSheet(fragment)
        }
    }

    private fun showBetaBottomSheet(fragment: Fragment){
        SettingsMainBetaBottomSheetDialogFragment().show(fragment.childFragmentManager, "bs_beta")
    }

    private fun showUpdateBottomSheet(fragment: Fragment) = viewModelScope.launch {
        val updateChecker by inject(UpdateChecker::class.java)
        updateChecker.getLatestRelease().collect {
            if(it != null && fragment.childFragmentManager.findFragmentByTag("bs_update") == null) {
                UpdateBottomSheetFragment().apply {
                    arguments = bundleOf(UpdateBottomSheetFragment.KEY_UPDATE to it)
                }.show(fragment.childFragmentManager, "bs_update")
            }
        }
    }

    sealed class ContainerState {
        data class Splash(val splashState: SplashState): ContainerState()
        object Setup: ContainerState()
        object Settings: ContainerState()
    }

    enum class SplashState {
        SHOWING, DONE
    }

    enum class SwitchState {
        MAIN, TRIPLE, HIDDEN
    }

}