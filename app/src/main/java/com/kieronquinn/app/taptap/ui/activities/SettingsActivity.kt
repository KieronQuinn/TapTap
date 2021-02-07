package com.kieronquinn.app.taptap.ui.activities

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.workers.UpdateCheckWorker
import dev.chrisbanes.insetter.Insetter

class SettingsActivity: AppCompatActivity() {

    private val isLightTheme
        get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Insetter.setEdgeToEdgeSystemUiFlags(window.decorView, true)
        if(isLightTheme){
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).or(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
        setContentView(R.layout.activity_setup)
        UpdateCheckWorker.queueCheckWorker(this)
    }

}