package com.kieronquinn.app.taptap.utils.extensions

import android.R
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import androidx.annotation.ColorInt
import com.google.android.material.switchmaterial.SwitchMaterial
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.toArgb
import com.kieronquinn.monetcompat.extensions.views.overrideRippleColor

/**
 *  Copied from MonetSwitch
 */
fun SwitchMaterial.applyMonet(monet: MonetCompat) = with(monet) {
    val uncheckedTrackColor = monet.getMonetColors().accent1[600]?.toArgb() ?: monet.getAccentColor(context, false)
    val checkedTrackColor = monet.getMonetColors().accent1[300]?.toArgb() ?: uncheckedTrackColor
    val checkedThumbColor = monet.getPrimaryColor(context, false)
    val uncheckedThumbColor = monet.getSecondaryColor(context, false)
    setTint(checkedTrackColor, uncheckedTrackColor, uncheckedThumbColor, checkedThumbColor)
}

private fun SwitchMaterial.setTint(@ColorInt checkedTrackColor: Int, @ColorInt unCheckedTrackColor: Int, @ColorInt uncheckedThumbColor: Int, @ColorInt checkedThumbColor: Int){
    trackTintList = ColorStateList(
        arrayOf(intArrayOf(R.attr.state_checked), intArrayOf()),
        intArrayOf(checkedTrackColor, unCheckedTrackColor)
    )
    val bgTintList = ColorStateList(
        arrayOf(intArrayOf(R.attr.state_checked), intArrayOf()),
        intArrayOf(checkedThumbColor, uncheckedThumbColor)
    )
    thumbTintList = bgTintList
    backgroundTintList = bgTintList
    backgroundTintMode = PorterDuff.Mode.SRC_ATOP
    overrideRippleColor(colorStateList = bgTintList)
}