package com.kieronquinn.app.taptap.fragments

import android.os.Bundle
import android.view.View
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.models.store.TripleTapActionListFile
import com.kieronquinn.app.taptap.utils.isTripleTapEnabled
import com.kieronquinn.app.taptap.utils.sharedPreferences
import kotlinx.android.synthetic.main.fragment_actions.*

class SettingsActionTripleFragment: BaseActionFragment() {

    override val actions by lazy {
        TripleTapActionListFile.loadFromFile(requireContext()).mapNotNull {
            try {
                if(it.action == null) null
                else it
            } catch (e: RuntimeException) {
                null
            }
        }.toMutableList()
    }

    override fun saveToFile() {
        TripleTapActionListFile.saveToFile(recyclerView.context, actions.toTypedArray(), sharedPreferences)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ic_empty_state.setImageResource(R.drawable.ic_actions_triple)
    }

    override fun onResume() {
        super.onResume()
        (activity as? SettingsActivity)?.run {
            setSwitchTag(SettingsActivity.TAG_SWITCH_TRIPLE_TAP)
            setSwitchChecked(isTripleTapEnabled)
            setSwitchVisible(true)
            setSwitchText(R.string.switch_triple)
        }
    }

}