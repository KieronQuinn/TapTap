package com.kieronquinn.app.taptap.fragments.action

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.models.TapActionCategory
import com.kieronquinn.app.taptap.utils.dip
import kotlinx.android.synthetic.main.fragment_add_action_category.*

class ActionCategoryInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_action_category_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnApplyWindowInsetsListener { v, insets ->
            v.layoutParams.apply {
                this as FrameLayout.LayoutParams
                bottomMargin = insets.systemWindowInsetBottom
            }
            insets
        }
    }

}