package com.kieronquinn.app.taptap.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.preferences.Preference
import com.kieronquinn.app.taptap.preferences.SliderPreference
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_NAME
import com.kieronquinn.app.taptap.utils.getToolbarHeight
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.android.synthetic.main.activity_settings.*

abstract class BaseSettingsFragment : PreferenceFragmentCompat(), View.OnScrollChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.applySystemWindowInsetsToPadding(top = true, bottom = true, left = true, right = true)
        listView.post {
            val topPadding = if(this is SettingsFragment) ((context?.getToolbarHeight() ?: 0) * 2) + resources.getDimension(R.dimen.margin_small).toInt()
            else context?.getToolbarHeight()
            listView.setPadding(listView.paddingLeft, listView.paddingTop + (topPadding ?: 0), listView.paddingRight, listView.paddingBottom)
            listView.setOnScrollChangeListener(this)
            listView.overScrollMode = View.OVER_SCROLL_NEVER
            listView.smoothScrollToPosition( 0)
        }
        setToolbarElevationEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        (activity as? SettingsActivity)?.setSwitchVisible(this is SettingsFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeAsUpEnabled(false)
    }

    private fun setToolbarElevationEnabled(enabled: Boolean){
        (activity as? SettingsActivity)?.setToolbarElevationEnabled(enabled)
    }

    internal fun setHomeAsUpEnabled(enabled: Boolean){
        (activity as? SettingsActivity)?.supportActionBar?.apply {
            //Re-apply the icon to fix theme switching
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayHomeAsUpEnabled(enabled)
        }
    }

    override fun onScrollChange(v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        setToolbarElevationEnabled(listView.computeVerticalScrollOffset() > 0)
    }

    fun getPreference(key: String, invoke: (Preference) -> Unit) {
        findPreference<Preference>(key)?.run(invoke)
    }

    fun getSwitchPreference(key: String, invoke: (SwitchPreference) -> Unit) {
        findPreference<SwitchPreference>(key)?.run(invoke)
    }

    fun getSliderPreference(key: String, invoke: (SliderPreference) -> Unit) {
        findPreference<SliderPreference>(key)?.run(invoke)
    }

    fun navigate(@IdRes navigationAction: Int, options: Bundle? = null){
        findNavController().navigate(navigationAction, options)
    }

}