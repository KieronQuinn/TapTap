package com.kieronquinn.app.taptap.components.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.XmlRes
import androidx.core.view.updatePadding
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.ui.preferences.Preference
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.ui.screens.container.ContainerViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

abstract class BaseSettingsFragment(@XmlRes private val resourceId: Int): PreferenceFragmentCompat(), TapFragment {

    private val containerViewModel by sharedViewModel<ContainerViewModel>()

    open val disableInsetAdjustment = false
    open val disableToolbarBackground = false

    private val scrollListener = object: RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            containerViewModel.updateScrollOffset(recyclerView.computeVerticalScrollOffset())
        }

    }

    abstract fun setupPreferences(preferenceScreen: PreferenceScreen)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(resourceId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPreferences(preferenceScreen)
        if(!disableInsetAdjustment) {
            containerViewModel.topInsetChange.observe(viewLifecycleOwner) {
                listView.run {
                    updatePadding(top = containerViewModel.getTopInset(view.context))
                    addOnScrollListener(scrollListener)
                    smoothScrollToPosition(0)
                    containerViewModel.updateScrollOffset(0)
                    overScrollMode = View.OVER_SCROLL_NEVER
                }
            }
            containerViewModel.navigationBarSize.observe(viewLifecycleOwner) {
                listView.updatePadding(bottom = it)
            }
        }else{
            listView.run {
                overScrollMode = View.OVER_SCROLL_NEVER
            }
        }
    }

    override fun onResume() {
        super.onResume()
        containerViewModel.shouldDisableToolbarBackground.postValue(disableToolbarBackground)
    }

    internal fun <T> Preference?.bindOnClick(onClickMethod: (T) -> Any?, arg: T){
        this?.let {
            setOnPreferenceClickListener {
                onClickMethod.invoke(arg)
                true
            }
        }
    }

}