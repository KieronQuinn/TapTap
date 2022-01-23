package com.kieronquinn.app.taptap.utils.dummy

import com.kieronquinn.app.taptap.utils.logging.InstanceId
import com.kieronquinn.app.taptap.utils.logging.UiEventLogger

class DummyUiEventLogger: UiEventLogger {

    override fun log(event: UiEventLogger.UiEventEnum) {
        //no-op
    }

    override fun log(event: UiEventLogger.UiEventEnum, uid: Int, packageName: String?) {
        //no-op
    }

    override fun logWithInstanceId(
        event: UiEventLogger.UiEventEnum,
        uid: Int,
        packageName: String?,
        instance: InstanceId?
    ) {
        //no-op
    }

    override fun logWithInstanceIdAndPosition(
        event: UiEventLogger.UiEventEnum,
        uid: Int,
        packageName: String?,
        instance: InstanceId?,
        position: Int
    ) {
        //no-op
    }

    override fun logWithPosition(
        event: UiEventLogger.UiEventEnum,
        uid: Int,
        packageName: String?,
        position: Int
    ) {
        //no-op
    }

}