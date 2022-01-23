package com.kieronquinn.app.taptap.components.navigation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import com.jakewharton.processphoenix.ProcessPhoenix
import com.kieronquinn.app.taptap.ui.activities.MainActivity
import com.kieronquinn.app.taptap.ui.activities.MainActivity.Companion.EXTRA_SUPPRESS_SPLASH_DELAY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect

interface BaseNavigation {

    val navigationBus: Flow<NavigationEvent>

    suspend fun navigate(navDirections: NavDirections)
    suspend fun navigate(@IdRes id: Int, arguments: Bundle? = null)
    suspend fun navigate(intent: Intent)
    suspend fun navigateUpTo(@IdRes id: Int, popInclusive: Boolean = false)
    suspend fun navigateBack()
    suspend fun restartActivity()
    suspend fun phoenix()

}

sealed class NavigationEvent {
    data class Directions(val directions: NavDirections): NavigationEvent()
    data class Id(@IdRes val id: Int, val arguments: Bundle? = null): NavigationEvent()
    data class PopupTo(@IdRes val id: Int, val popInclusive: Boolean): NavigationEvent()
    data class Intent(val intent: android.content.Intent, val onActivityNotFound: (() -> Unit)? = null): NavigationEvent()
    object Back: NavigationEvent()
    object RestartActivity: NavigationEvent()
    object Phoenix: NavigationEvent()
}

open class NavigationImpl: BaseNavigation {

    private val _navigationBus = MutableSharedFlow<NavigationEvent>()
    override val navigationBus = _navigationBus.asSharedFlow()

    override suspend fun navigate(id: Int, arguments: Bundle?) {
        _navigationBus.emit(NavigationEvent.Id(id, arguments))
    }

    override suspend fun navigate(navDirections: NavDirections) {
        _navigationBus.emit(NavigationEvent.Directions(navDirections))
    }

    override suspend fun navigateBack() {
        _navigationBus.emit(NavigationEvent.Back)
    }

    override suspend fun navigateUpTo(id: Int, popInclusive: Boolean) {
        _navigationBus.emit(NavigationEvent.PopupTo(id, popInclusive))
    }

    override suspend fun navigate(intent: Intent) {
        _navigationBus.emit(NavigationEvent.Intent(intent))
    }

    override suspend fun restartActivity() {
        _navigationBus.emit(NavigationEvent.RestartActivity)
    }

    override suspend fun phoenix() {
        _navigationBus.emit(NavigationEvent.Phoenix)
    }

}

//Root level navigation for full screen changes
interface RootNavigation: BaseNavigation
class RootNavigationImpl: NavigationImpl(), RootNavigation

//Container level navigation for inner changes
interface ContainerNavigation: BaseNavigation
class ContainerNavigationImpl: NavigationImpl(), ContainerNavigation

//Gesture Configuration bottom sheet navigation
interface GestureConfigurationNavigation: BaseNavigation
class GestureConfigurationNavigationImpl: NavigationImpl(), GestureConfigurationNavigation

suspend fun NavHostFragment.setupWithNavigation(navigation: BaseNavigation) {
    navigation.navigationBus.collect {
        when(it){
            is NavigationEvent.Directions -> navController.navigateSafely(it.directions)
            is NavigationEvent.Id -> navController.navigateSafely(it.id, it.arguments ?: Bundle.EMPTY)
            is NavigationEvent.Back -> navController.navigateUp()
            is NavigationEvent.PopupTo -> navController.popBackStack(it.id, it.popInclusive)
            is NavigationEvent.Intent -> try {
                startActivity(it.intent)
            }catch (e: ActivityNotFoundException){
                it.onActivityNotFound?.invoke()
            }
            is NavigationEvent.RestartActivity -> {
                requireActivity().let { activity ->
                    activity.finish()
                    activity.startActivity(activity.intent)
                }
            }
            is NavigationEvent.Phoenix -> {
                ProcessPhoenix.triggerRebirth(requireContext(), Intent(requireContext(), MainActivity::class.java).apply {
                    putExtra(EXTRA_SUPPRESS_SPLASH_DELAY, true)
                })
            }
        }
    }
}

private fun NavController.navigateSafely(directions: NavDirections){
    currentDestination?.getAction(directions.actionId)?.let {
        if(it.destinationId != currentDestination?.id) {
            navigate(directions)
        }
    }
}

private fun NavController.navigateSafely(@IdRes action: Int, arguments: Bundle){
    currentDestination?.getAction(action)?.let {
        if(it.destinationId != currentDestination?.id) {
            navigate(action, arguments)
        }
    }
}