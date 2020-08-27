package com.kieronquinn.app.taptap.activities

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.fragments.BaseFragment
import com.kieronquinn.app.taptap.utils.animateColorChange
import com.kieronquinn.app.taptap.utils.animateElevationChange
import com.kieronquinn.app.taptap.utils.dip
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private val isLightTheme
        get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES

    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = ""
            setHomeAsUpIndicator(R.drawable.ic_back)
        }
        navController.addOnDestinationChangedListener(this)
        Insetter.setEdgeToEdgeSystemUiFlags(window.decorView, true)
        if(isLightTheme){
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).or(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
        toolbar.applySystemWindowInsetsToPadding(top = true)
        setToolbarElevationEnabled(false)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        sensorManager.registerListener(object: SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent?) {
                Log.d("TapProximity", "Proximity state ${event!!.values!![0]}")
            }

        }, proximity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        toolbar_title?.text = destination.label
    }

    private var isToolbarElevationEnabled = false
    private var toolbarColorAnimation: ValueAnimator? = null
    private var toolbarElevationAnimation: ValueAnimator? = null

    fun setToolbarElevationEnabled(enabled: Boolean){
        if(enabled == isToolbarElevationEnabled) return
        isToolbarElevationEnabled = enabled
        val toolbarColor = if(enabled){
            ContextCompat.getColor(this, R.color.toolbarColor)
        }else{
            ContextCompat.getColor(this, R.color.windowBackground)
        }
        val elevation = if(enabled) dip(8).toFloat() else 0f
        val initialBeforeColor = if(toolbarColorAnimation == null){
            ContextCompat.getColor(this, R.color.toolbarColor)
        }else{
            null
        }

        toolbarColorAnimation?.cancel()
        toolbarElevationAnimation?.cancel()
        toolbarColorAnimation = toolbar.animateColorChange(beforeColor = initialBeforeColor, afterColor = toolbarColor)
        toolbarElevationAnimation = toolbar.animateElevationChange(elevation)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> {
                navController.navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (navHostFragment?.childFragmentManager != null) {
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]
            val result = (currentFragment as? BaseFragment)?.onBackPressed()
            if (result != true) super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

}