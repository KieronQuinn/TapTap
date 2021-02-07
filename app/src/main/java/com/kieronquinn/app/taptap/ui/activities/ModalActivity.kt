package com.kieronquinn.app.taptap.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.kieronquinn.app.taptap.R
import dev.chrisbanes.insetter.Insetter

class ModalActivity: AppCompatActivity() {

    companion object {
        const val KEY_NAV_GRAPH = "nav_graph"
    }

    private val isLightTheme
        get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Insetter.setEdgeToEdgeSystemUiFlags(window.decorView, true)
        if(isLightTheme){
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).or(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
        val navId = intent.getIntExtra(KEY_NAV_GRAPH, 0)
        if(navId == 0){
            finish()
            return
        }
        setContentView(R.layout.activity_modal)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val graph = navHostFragment.navController.navInflater.inflate(navId)
        navHostFragment.navController.graph = graph
    }

}