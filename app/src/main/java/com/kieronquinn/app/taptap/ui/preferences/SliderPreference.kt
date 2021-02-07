package com.kieronquinn.app.taptap.ui.preferences

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.slider.Slider
import com.kieronquinn.app.taptap.R

class SliderPreference : Preference {

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    private lateinit var slider: Slider

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val titleView = holder?.findViewById(android.R.id.title) as TextView
        titleView.isSingleLine = false
        titleView.typeface = ResourcesCompat.getFont(context, R.font.hkgrotesk)
        val summaryView = holder.findViewById(android.R.id.summary) as? TextView
        summaryView?.maxLines = Int.MAX_VALUE
        val container = summaryView?.parent as RelativeLayout
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        slider = layoutInflater.inflate(R.layout.preference_widget_slider, container, false) as Slider
        slider.apply {
        }
        slider.updateLayoutParams<RelativeLayout.LayoutParams> {
            addRule(RelativeLayout.BELOW, android.R.id.summary)
        }
        container.addView(slider)
        val root = summaryView?.parent?.parent as LinearLayout
        root.background = null
    }

    fun getSlider(): Slider {
        return slider
    }

}