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

class ActionCategoryFragment : Fragment() {

    private val navController by lazy {
        findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_action_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_action_category_launch.setOnClickListener {
            moveToCategory(TapActionCategory.LAUNCH)
        }
        add_action_category_utilities.setOnClickListener {
            moveToCategory(TapActionCategory.UTILITIES)
        }
        add_action_category_actions.setOnClickListener {
            moveToCategory(TapActionCategory.ACTIONS)
        }
        add_action_category_advanced.setOnClickListener {
            moveToCategory(TapActionCategory.ADVANCED)
        }
        view.setOnApplyWindowInsetsListener { v, insets ->
            v.layoutParams.apply {
                this as FrameLayout.LayoutParams
                bottomMargin = insets.systemWindowInsetBottom
            }
            insets
        }
    }

    private fun moveToCategory(category: TapActionCategory){
        val bundle = Bundle()
        bundle.putSerializable("category", category)
        navController.navigate(R.id.action_actionCategoryFragment_to_actionListFragment, bundle)
    }

}