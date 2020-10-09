package com.kieronquinn.app.taptap.fragments.setup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP
import com.kieronquinn.app.taptap.utils.sharedPreferences
import kotlinx.android.synthetic.main.fragment_setup_landing.*

class LandingFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        landing_button_get_started.setOnClickListener {
            findNavController().navigate(R.id.action_landingFragment_to_gestureConfigurationFragment)
        }
        landing_button_skip_setup.setOnClickListener {
            sharedPreferences?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP, true)?.apply()
            findNavController().navigate(R.id.action_landingFragment_to_settingsActivity)
            activity?.finish()
        }
    }

}