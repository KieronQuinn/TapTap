package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R

enum class TapActionCategory(@StringRes val labelRes: Int, @DrawableRes val icon: Int) {
    LAUNCH(R.string.add_action_category_launch, R.drawable.ic_action_category_launch),
    UTILITIES(R.string.add_action_category_utilities, R.drawable.ic_action_category_utilities),
    ACTIONS(R.string.add_action_category_actions, R.drawable.ic_action_back),
    ADVANCED(R.string.add_action_category_advanced, R.drawable.ic_action_category_advanced)
}