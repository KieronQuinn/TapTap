package com.kieronquinn.app.taptap.impl

import com.android.systemui.statusbar.policy.KeyguardStateController
import de.robv.android.xposed.XposedHelpers

class KeyguardStateControllerImpl : KeyguardStateController {

    private var keyguardViewMediator: Any? = null

    fun setMediator(mediator: Any?){
        keyguardViewMediator = mediator
    }

    override fun getKeyguardFadingAwayDelay(): Long {
        return 0
    }

    override fun calculateGoingToFullShadeDelay(): Long {
        return 0
    }

    override fun removeCallback(arg1: Any?) {
    }

    override fun isOccluded(): Boolean {
        keyguardViewMediator?.let {
            return keyguardViewMediatorGetOccluded(it)
        } ?: run {
            return false
        }
    }

    override fun isMethodSecure(): Boolean {
        return true
    }

    override fun canDismissLockScreen(): Boolean {
        return false
    }

    override fun isKeyguardFadingAway(): Boolean {
        return false
    }

    override fun isShowing(): Boolean {
        keyguardViewMediator?.let {
            return keyguardViewMediatorGetShowing(it)
        } ?: run {
            return false
        }
    }

    override fun isKeyguardGoingAway(): Boolean {
        return false
    }

    override fun addCallback(arg1: Any?) {
    }

    override fun isLaunchTransitionFadingAway(): Boolean {
        return false
    }

    override fun getKeyguardFadingAwayDuration(): Long {
        return 0
    }

    private fun keyguardViewMediatorGetOccluded(keyguardViewMediator: Any): Boolean {
        return XposedHelpers.getBooleanField(keyguardViewMediator, "mOccluded")
    }

    private fun keyguardViewMediatorGetShowing(keyguardViewMediator: Any): Boolean {
        return XposedHelpers.getBooleanField(keyguardViewMediator, "mShowing")
    }
}