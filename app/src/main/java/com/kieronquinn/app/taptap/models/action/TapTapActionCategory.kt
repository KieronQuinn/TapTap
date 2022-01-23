package com.kieronquinn.app.taptap.models.action

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R

enum class TapTapActionCategory(@StringRes val labelRes: Int, @StringRes val descRes: Int, @DrawableRes val icon: Int) {
    LAUNCH(R.string.add_action_category_launch, R.string.add_action_category_launch_desc, R.drawable.ic_action_category_launch),
    UTILITIES(R.string.add_action_category_utilities, R.string.add_action_category_utilities_desc, R.drawable.ic_action_category_utilities),
    BUTTON(R.string.add_action_category_button, R.string.add_action_category_button_desc, R.drawable.ic_action_category_button),
    SOUND(R.string.add_action_category_sound, R.string.add_action_category_sound_desc, R.drawable.ic_action_category_sound),
    GESTURE(R.string.add_action_category_gesture, R.string.add_action_category_gesture_desc, R.drawable.ic_action_category_gesture),
    ACCESSIBILITY(R.string.add_action_category_accessibility, R.string.add_action_category_accessibility_desc, R.drawable.ic_action_chip_accessibility),
    ADVANCED(R.string.add_action_category_advanced, R.string.add_action_category_advanced_desc, R.drawable.ic_action_category_advanced)
}