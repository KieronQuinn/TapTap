package com.kieronquinn.app.taptap.ui.activities

import android.content.res.Configuration
import android.os.Build
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.ActivityReachabilityBinding
import com.kieronquinn.app.taptap.ui.base.BoundActivity
import com.kieronquinn.app.taptap.ui.screens.reachability.ReachabilityFragment

class ReachabilityActivity: BoundActivity<ActivityReachabilityBinding>(ActivityReachabilityBinding::inflate) {

    companion object {
        const val KEY_PACKAGE_NAME = "package_name"
    }

    private val app by lazy {
        intent.getStringExtra(KEY_PACKAGE_NAME)
    }

    private val fragment
        get() = supportFragmentManager.findFragmentById(R.id.fragment_reachability) as? ReachabilityFragment

    override fun onWindowAttributesChanged(params: WindowManager.LayoutParams?) {
        super.onWindowAttributesChanged(params)
        fragment?.onWindowAttributesChanged(window?.decorView?.height ?: return)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        if(!isInMultiWindowMode) {
            fragment?.onExitMultiWindow(app)
        }
    }

}