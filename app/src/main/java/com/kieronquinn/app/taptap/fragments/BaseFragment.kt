package com.kieronquinn.app.taptap.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.utils.getToolbarHeight
import com.kieronquinn.app.taptap.utils.isMainEnabled
import com.kieronquinn.app.taptap.utils.isTripleTapEnabled
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding

abstract class BaseFragment : Fragment() {

    internal fun setupRecyclerView(recyclerView: RecyclerView, extraTopPadding: Int = 0, extraBottomPadding: Int = 0, offsetForSystem: Boolean = false){
        recyclerView.applySystemWindowInsetsToPadding(top = true, bottom = true, left = true, right = true)
        recyclerView.post {
            recyclerView.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop + (context?.getToolbarHeight() ?: 0) + extraTopPadding, if(offsetForSystem) 0 else recyclerView.paddingRight, recyclerView.paddingBottom + extraBottomPadding)
            recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
                setToolbarElevationEnabled(recyclerView.computeVerticalScrollOffset() > 0)
            }
            recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
            recyclerView.smoothScrollToPosition( 0)
        }
        setToolbarElevationEnabled(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHomeAsUpEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        (activity as? SettingsActivity)?.run {
            setSwitchVisible(false)
        }
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

    open fun onBackPressed(): Boolean {
        //Do nothing unless overridden
        return false
    }

}