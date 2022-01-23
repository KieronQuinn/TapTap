package com.kieronquinn.app.taptap.ui.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.ui.screens.root.RootSharedViewModel
import com.kieronquinn.app.taptap.utils.extensions.delayPreDrawUntilFlow
import com.kieronquinn.app.taptap.work.TapTapUpdateCheckWorker
import com.kieronquinn.monetcompat.app.MonetCompatActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity: MonetCompatActivity() {

    companion object {
        const val EXTRA_SUPPRESS_SPLASH_DELAY = "suppress_splash_delay"
    }

    private val rootViewModel by viewModel<RootSharedViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !intent.getBooleanExtra(EXTRA_SUPPRESS_SPLASH_DELAY, false)) {
            //TODO move when android.core:splashscreen supports animations
            findViewById<View>(android.R.id.content).delayPreDrawUntilFlow(rootViewModel.appReady, lifecycle)
        }
        lifecycleScope.launchWhenCreated {
            monet.awaitMonetReady()
            setContentView(R.layout.activity_main)
        }
        TapTapUpdateCheckWorker.queueCheckWorker(this)
    }

}