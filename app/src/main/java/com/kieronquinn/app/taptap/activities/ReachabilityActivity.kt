package com.kieronquinn.app.taptap.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.columbus.actions.LaunchReachability
import com.kieronquinn.app.taptap.utils.sharedPreferences
import kotlinx.android.synthetic.main.activity_reachability.*

class ReachabilityActivity : AppCompatActivity() {

    companion object {
        var isRunning = false
        const val KEY_PACKAGE_NAME = "package_name"
        const val KEY_REACHABILITY_LEFT_HANDED = "reachability_left_handed"
    }

    private var isLeftHanded = false

    private var windowReady = false

    private val minButtonHeight by lazy {
        resources.getDimension(R.dimen.reachability_button_size) + resources.getDimension(R.dimen.activity_padding) + resources.getDimension(R.dimen.activity_padding)
    }

    override fun onStart() {
        super.onStart()
        isRunning = true
    }

    override fun onStop() {
        super.onStop()
        isRunning = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(sharedPreferences?.contains(KEY_REACHABILITY_LEFT_HANDED) == false){
            Toast.makeText(this, R.string.reachability_left_handed_info, Toast.LENGTH_LONG).show()
            sharedPreferences?.edit()?.putBoolean(KEY_REACHABILITY_LEFT_HANDED, isLeftHanded)?.apply()
        }
        isLeftHanded = sharedPreferences?.getBoolean(KEY_REACHABILITY_LEFT_HANDED, false) ?: false
        sendBroadcast(Intent(LaunchReachability.INTENT_ACTION_START_SPLIT_SCREEN).apply {
            setPackage(packageName)
        })
        setContentView(R.layout.activity_reachability)
        reachability_notifications.setOnClickListener {
            sendBroadcast(Intent(LaunchReachability.INTENT_ACTION_SHOW_NOTIFICATIONS).apply {
                setPackage(packageName)
            })
        }
        reachability_notifications.setOnLongClickListener {
            sharedPreferences?.edit()?.putBoolean(KEY_REACHABILITY_LEFT_HANDED, !isLeftHanded)?.apply()
            isLeftHanded = !isLeftHanded
            updateHandedness()
            true
        }
        reachability_quick_settings.setOnClickListener {
            sendBroadcast(Intent(LaunchReachability.INTENT_ACTION_SHOW_QUICK_SETTINGS).apply {
                setPackage(packageName)
            })
        }
        updateHandedness()
        windowReady = true
    }

    private fun updateHandedness(){
        if(isLeftHanded){
            reachability_container.gravity = Gravity.BOTTOM or Gravity.START
        }else{
            reachability_container.gravity = Gravity.BOTTOM or Gravity.END
        }
    }

    override fun onWindowAttributesChanged(params: WindowManager.LayoutParams?) {
        super.onWindowAttributesChanged(params)
        if(!windowReady) return
        if(window.decorView.height < minButtonHeight){
            reachability_notifications.visibility = View.GONE
            reachability_quick_settings.visibility = View.GONE
        }else{
            reachability_notifications.visibility = View.VISIBLE
            reachability_quick_settings.visibility = View.VISIBLE
        }
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration?) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        if(!isInMultiWindowMode){
            val otherApp = intent.getStringExtra(KEY_PACKAGE_NAME)
            otherApp?.let {
                packageManager.getLaunchIntentForPackage(it)?.run {
                    startActivity(this)
                }
            }
            sendBroadcast(Intent(LaunchReachability.INTENT_ACTION_ENDING).apply {
                setPackage(packageName)
            })
            finishAndRemoveTask()
        }
    }

}