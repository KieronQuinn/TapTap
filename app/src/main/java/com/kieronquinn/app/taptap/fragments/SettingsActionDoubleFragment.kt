package com.kieronquinn.app.taptap.fragments

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.adapters.ActionAdapter
import com.kieronquinn.app.taptap.fragments.bottomsheets.ActionBottomSheetFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.GateBottomSheetFragment
import com.kieronquinn.app.taptap.fragments.bottomsheets.GenericBottomSheetFragment
import com.kieronquinn.app.taptap.fragments.gate.GateListFragment
import com.kieronquinn.app.taptap.models.*
import com.kieronquinn.app.taptap.models.store.DoubleTapActionListFile
import com.kieronquinn.app.taptap.utils.*
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_actions.*
import kotlinx.android.synthetic.main.item_action.view.*
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