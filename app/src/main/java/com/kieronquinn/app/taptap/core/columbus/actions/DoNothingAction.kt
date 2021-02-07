package com.kieronquinn.app.taptap.core.columbus.actions

import android.content.Context
import com.google.android.systemui.columbus.actions.Action
import com.kieronquinn.app.taptap.core.TapSharedPreferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/*
    This action exists solely to be added by the service to the double tap list when the list is empty
    It's only available when triple tap is enabled, so the listeners get disabled when triple tap is disabled
 */
class DoNothingAction(context: Context, private val forceEnable: Boolean = false) : Action(context, emptyList()), KoinComponent {
    private val tapSharedPreferences by inject<TapSharedPreferences>()

    override fun isAvailable(): Boolean = forceEnable || tapSharedPreferences.isTripleTapEnabled
}