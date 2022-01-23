package com.kieronquinn.app.taptap.utils.dummy

import com.kieronquinn.app.taptap.utils.statusbar.StatusBarStateController

class DummyStatusBarStateController: StatusBarStateController {

    override val isDozing: Boolean
        get() = false

    override fun addCallback(listener: StatusBarStateController.StateListener?) {
        //no-op
    }

}