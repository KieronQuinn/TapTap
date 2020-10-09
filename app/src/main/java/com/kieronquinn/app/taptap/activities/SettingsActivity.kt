package com.kieronquinn.app.taptap.activities

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.fragments.BaseFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.MaterialBottomSheetDialogFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.UpdateBottomSheetFragment
import com.kieronquinn.app.taptap.fragments.setup.BaseSetupFragment
import com.kieronquinn.app.taptap.utils.*
import com.kieronquinn.app.taptap.workers.UpdateCheckWorker
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_setup.*
import kotlin.math.max

class SettingsActivity : AppCompatActivity(), NavController.OnDestinationChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        const val TAG_SWITCH_MAIN = "switch_main"
        const val TAG_SWITCH_TRIPLE_TAP = "switch_triple_tap"
        private const val HAS_RUN_ANIMATION = "has_run_animation"
        const val FORCE_RERUN_SETUP = "force_rerun_setup"
    }

    private val fragmentContainer: FrameLayout by lazy {
        fragment_container
    }

    private val forceRerunSetup by lazy {
        intent.getBooleanExtra(FORCE_RERUN_SETUP, false)
    }

    private val isLightTheme
        get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES

    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    private var isFirstStart = true

    val updateChecker by lazy { UpdateChecker() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState?.getBoolean(HAS_RUN_ANIMATION, false) == true) isFirstStart = false
        if(!doesDeviceHaveGyroscope()){
            startActivity(Intent(this, ModalActivity::class.java).apply {
                putExtra(ModalActivity.KEY_NAV_GRAPH, R.navigation.nav_graph_modal_no_gyroscope)
            })
            finish()
            return
        }
        if(hasSeenSetup && !forceRerunSetup){
            showSettingsUi()
        }else{
            startSetupFlow(savedInstanceState)
        }
    }

    private fun showSettingsUi(){
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
        switch_main.applySystemWindowInsetsToMargin(top = true)
        setToolbarElevationEnabled(false)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        switch_main.isChecked = isMainEnabled
        switch_main.setOnCheckedChangeListener(checkListener)
        updateChecker.getLatestRelease { success, updateChecker ->
            if(success){
                updateChecker.newUpdate.value?.let {
                    //New update available!
                    showUpdateBottomSheet(it)
                }
            }else{
                Toast.makeText(this, getString(R.string.update_error), Toast.LENGTH_LONG).show()
            }
        }
        UpdateCheckWorker.queueCheckWorker(this)
    }

    fun showUpdateBottomSheet(update: UpdateChecker.Update? = updateChecker.newUpdate.value, force: Boolean = false) {
        update ?: return
        if(!force && !isFirstStart) return
        MaterialBottomSheetDialogFragment.create(UpdateBottomSheetFragment().apply {
            arguments = bundleOf(UpdateBottomSheetFragment.KEY_UPDATE to update)
        }, supportFragmentManager, "bs_update"){}
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
        switch_main.animateColorChange(beforeColor = initialBeforeColor, afterColor = toolbarColor)
        toolbarElevationAnimation = toolbar.animateElevationChange(elevation)
        switch_main.animateElevationChange(elevation)
    }

    fun setSwitchVisible(visible: Boolean){
        switch_main?.visibility = if(visible) View.VISIBLE else View.GONE
    }

    fun setSwitchChecked(checked: Boolean){
        switch_main?.isChecked = checked
    }

    fun setSwitchText(@StringRes textRes: Int){
        switch_main?.text = getString(textRes)
    }

    fun setSwitchTag(tag: String){
        switch_main?.tag = tag
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                navController.navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startSetupFlow(savedInstanceState: Bundle?){
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_setup)
        if(resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES){
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR).or(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
        }
        window.decorView.setEdgeToEdgeSystemUiFlags(true)
        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT
        val hasRunAnimation = savedInstanceState?.getBoolean(HAS_RUN_ANIMATION, false) ?: false
        if(!hasRunAnimation) {
            lottieAnimation.addValueCallback(KeyPath("**"), LottieProperty.COLOR_FILTER, {
                PorterDuffColorFilter(
                    ContextCompat.getColor(this, R.color.colorLottie),
                    PorterDuff.Mode.SRC_ATOP
                )
            })
            var hasStartedReveal = false
            lottieAnimation.addAnimatorUpdateListener {
                if (!hasStartedReveal && lottieAnimation.frame >= 100) {
                    hasStartedReveal = true
                    startCircularRevealAndHideLottie()
                }
            }
            lottieAnimation.setAnimation(R.raw.double_tap)
            lottieAnimation.playAnimation()
        }else{
            lottieAnimation.visibility = View.GONE
            lottieTitle.visibility = View.GONE
            fragmentContainer.visibility = View.VISIBLE
        }
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

    private val checkListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(buttonView.tag == TAG_SWITCH_MAIN) {
                sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, isChecked).apply()
            }else{
                sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH, isChecked).apply()
            }
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == SHARED_PREFERENCES_KEY_MAIN_SWITCH && switch_main?.tag == TAG_SWITCH_MAIN) {
            switch_main.setOnCheckedChangeListener(null)
            switch_main.isChecked = isMainEnabled
            switch_main.setOnCheckedChangeListener(checkListener)
        }else if(key == SHARED_PREFERENCES_KEY_TRIPLE_TAP_SWITCH && switch_main?.tag == TAG_SWITCH_TRIPLE_TAP) {
            switch_main.setOnCheckedChangeListener(null)
            switch_main.isChecked = isTripleTapEnabled
            switch_main.setOnCheckedChangeListener(checkListener)
        }
    }

    private fun startCircularRevealAndHideLottie(){
        fragmentContainer.visibility = View.VISIBLE
        val circularReveal = ViewAnimationUtils.createCircularReveal(fragmentContainer, fragmentContainer.centerX.toInt(), fragmentContainer.centerY.toInt(), 0f, max(fragmentContainer.measuredHeight.toFloat(), fragmentContainer.measuredWidth.toFloat())).apply {
            duration *= 2
        }
        lottieAnimation.apply {
            val animation = AlphaAnimation(1f, 0f)
            animation.duration = circularReveal.duration
            animation.interpolator = AccelerateDecelerateInterpolator()
            animation.fillAfter = true
            startAnimation(animation)
        }
        lottieTitle.apply {
            val animation = AlphaAnimation(1f, 0f)
            animation.duration = circularReveal.duration
            animation.interpolator = AccelerateDecelerateInterpolator()
            animation.fillAfter = true
            startAnimation(animation)
        }
        circularReveal.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(HAS_RUN_ANIMATION, true)
    }

    private fun onHomeAsUpPressed(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (navHostFragment?.childFragmentManager != null) {
            val currentFragment = navHostFragment.childFragmentManager.fragments[0]
            val result = (currentFragment as? BaseSetupFragment)?.onHomeAsUpPressed()
            if (result != true){
                if(!findNavController(R.id.nav_host_fragment).navigateUp()){
                    finishAfterTransition()
                }
            }
        } else {
            if(!findNavController(R.id.nav_host_fragment).navigateUp()){
                finishAfterTransition()
            }
        }
    }

    private fun doesDeviceHaveGyroscope(): Boolean {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
    }

}