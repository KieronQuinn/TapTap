package com.kieronquinn.app.taptap.core

import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.core.services.TapForegroundService
import com.kieronquinn.app.taptap.core.services.TapGestureAccessibilityService

//Since we can't create the services directly, this container holds the instances while they're running
class TapServiceContainer {

    var accessibilityService: TapAccessibilityService? = null
    var gestureAccessibilityService: TapGestureAccessibilityService? = null
    var foregroundService: TapForegroundService? = null

}