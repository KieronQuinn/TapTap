package com.kieronquinn.app.taptap.activities

import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.fragments.BaseFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.UpdateBottomSheetFragment
import com.kieronquinn.app.taptap.utils.*
import com.kieronquinn.app.taptap.workers.UpdateCheckWorker
import dev.chrisbanes.insetter.Insetter
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), NavController.OnDestinationChangedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val isLightTheme
        get() = resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES

    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }

    val updateChecker by lazy { UpdateChecker() }

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

    fun showUpdateBottomSheet(update: UpdateChecker.Update? = updateChecker.newUpdate.value) {
        update ?: return
        UpdateBottomSheetFragment().apply {
            arguments = bundleOf(UpdateBottomSheetFragment.KEY_UPDATE to update)
        }.show(supportFragmentManager, "bs_update")
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

    private val checkListener =
        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            sharedPreferences.edit().putBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, isChecked).apply()
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key == SHARED_PREFERENCES_KEY_MAIN_SWITCH) {
            switch_main.setOnCheckedChangeListener(null)
            switch_main.isChecked = isMainEnabled
            switch_main.setOnCheckedChangeListener(checkListener)
        }
    }

}