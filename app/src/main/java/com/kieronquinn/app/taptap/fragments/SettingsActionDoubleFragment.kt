package com.kieronquinn.app.taptap.fragments

import com.kieronquinn.app.taptap.models.store.DoubleTapActionListFile
import com.kieronquinn.app.taptap.utils.*
import java.lang.RuntimeException

class SettingsActionDoubleFragment: BaseActionFragment() {

    override val actions by lazy {
        DoubleTapActionListFile.loadFromFile(requireContext()).mapNotNull {
            try {
                if(it.action == null) null
                else it
            } catch (e: RuntimeException) {
                null
            }
        }.toMutableList()
    }

    override fun saveToFile() {
        DoubleTapActionListFile.saveToFile(recyclerView.context, actions.toTypedArray(), sharedPreferences)
    }

}