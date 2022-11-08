package com.kieronquinn.app.taptap.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.ColorInt
import com.google.android.material.materialswitch.MaterialSwitch
import com.kieronquinn.monetcompat.R
import com.kieronquinn.monetcompat.core.MonetCompat
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import com.kieronquinn.monetcompat.extensions.views.overrideRippleColor
import com.kieronquinn.monetcompat.interfaces.MonetColorsChangedListener
import dev.kdrag0n.monet.theme.ColorScheme

/**
 *  A full-width Switch designed to look like the primary ones in Android 12's Settings app. It has
 *  its own background, tinted to Monet's colors, with the [Switch] thumb set to the same color,
 *  and the track a darker color. The background/track color changes depending on the switch state.
 */
open class MonetSwitch: FrameLayout, MonetColorsChangedListener {

    constructor(context: Context): super(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet, R.attr.switchStyle) {
        readAttributes(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet?, styleResId: Int): super(context, attributeSet, styleResId){
        readAttributes(attributeSet)
    }

    private val layoutInflater = LayoutInflater.from(context)

    private val layout by lazy {
        layoutInflater.inflate(com.kieronquinn.app.taptap.R.layout.view_monet_switch, this, false)
    }

    private val text by lazy {
        layout.findViewById<TextView>(com.kieronquinn.app.taptap.R.id.view_monet_switch_text)
    }

    private val switch by lazy {
        layout.findViewById<MaterialSwitch>(com.kieronquinn.app.taptap.R.id.view_monet_switch_switch)
    }

    private val root by lazy {
        layout.findViewById<LinearLayout>(com.kieronquinn.app.taptap.R.id.view_monet_switch_root)
    }

    var isChecked: Boolean
        get() = switch.isChecked
        set(value) {
            switch.isChecked = value
        }

    @SuppressLint("PrivateResource", "CustomViewStyleable")
    private fun readAttributes(attributeSet: AttributeSet?){
        if(attributeSet == null) return
        addView(layout)
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MonetSwitch)
        var textAppearance = typedArray.getResourceId(R.styleable.MonetSwitch_android_textAppearance, R.style.TextAppearance_AppCompat_Medium)
        //Sometimes the field will default to TextAppearance.Material so we need to counter that
        if(textAppearance == android.R.style.TextAppearance_Material) textAppearance = R.style.TextAppearance_AppCompat_Medium
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            text.setTextAppearance(textAppearance)
        }else{
            text.setTextAppearance(context, textAppearance)
        }
        val materialTypedArray = context.obtainStyledAttributes(attributeSet, com.google.android.material.R.styleable.MaterialSwitch)
        val thumbIcon = materialTypedArray.getResourceId(com.google.android.material.R.styleable.MaterialSwitch_thumbIcon, 0)
        val thumbTint = materialTypedArray.getColor(com.google.android.material.R.styleable.MaterialSwitch_thumbIconTint, 0)
        val thumbTintMode = materialTypedArray.getInt(com.google.android.material.R.styleable.MaterialSwitch_thumbIconTintMode, 0)
        if(thumbIcon != 0){
            switch.setThumbIconResource(thumbIcon)
        }
        if(thumbTint != 0){
            switch.thumbIconTintList = ColorStateList.valueOf(thumbTint)
        }
        if(thumbTintMode != 0){
            switch.thumbIconTintMode = PorterDuff.Mode.values()[thumbTintMode]
        }
        val textColor = typedArray.getColor(R.styleable.MonetSwitch_android_textColor, Color.BLACK)
        text.setTextColor(textColor)
        val switchText = typedArray.getText(R.styleable.MonetSwitch_android_text) ?: ""
        text.text = switchText
        typedArray.recycle()
        materialTypedArray.recycle()
    }

    private val monet by lazy {
        MonetCompat.getInstance()
    }

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if(!isInEditMode) {
            monet.addMonetColorsChangedListener(this, true)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        monet.removeMonetColorsChangedListener(this)
    }

    override fun onMonetColorsChanged(
        monet: MonetCompat,
        monetColors: ColorScheme,
        isInitialChange: Boolean
    ) {
        applyMonet()
    }

    private fun applyMonet() = with(monet) {
        val checkedThumbColor = monet.getPrimaryColor(context, false)
        val uncheckedThumbColor = monet.getSecondaryColor(context, false)
        setTint(uncheckedThumbColor, checkedThumbColor)
    }

    private fun setTint(
        @ColorInt uncheckedThumbColor: Int,
        @ColorInt checkedThumbColor: Int
    ){
        val bgTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf()),
            intArrayOf(checkedThumbColor, uncheckedThumbColor)
        )
        root.backgroundTintList = bgTintList
        root.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
        root.isActivated = switch.isChecked
        switch.setOnCheckedChangeListener { _, _ ->
            root.isActivated = switch.isChecked
        }
        switch.setOnClickListener {
            performClick()
        }
        switch.applyMonet()
        switch.thumbTintMode = PorterDuff.Mode.SRC_ATOP
        overrideRippleColor(colorStateList = bgTintList)
    }

}