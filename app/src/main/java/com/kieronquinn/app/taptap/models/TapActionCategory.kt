package com.kieronquinn.app.taptap.models

import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R

enum class TapActionCategory(@StringRes val labelRes: Int) {
    LAUNCH(R.string.add_action_category_launch),
    UTILITIES(R.string.add_action_category_utilities),
    ACTIONS(R.string.add_action_category_actions),
    ADVANCED(R.string.add_action_category_advanced)
}