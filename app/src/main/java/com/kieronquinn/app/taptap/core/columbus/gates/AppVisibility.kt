package com.kieronquinn.app.taptap.core.columbus.gates

import android.content.Context
import com.google.android.systemui.columbus.gates.Gate
import com.kieronquinn.app.taptap.core.TapServiceContainer
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class AppVisibility(context: Context, private val packageName: String) : Gate(context), KoinComponent {

    private val tapServiceContainer by inject<TapServiceContainer>()

    override fun onActivate() {}
    override fun onDeactivate() {}
    override fun isBlocked(): Boolean {
        return tapServiceContainer.accessibilityService?.getCurrentPackageName() == packageName
    }

}