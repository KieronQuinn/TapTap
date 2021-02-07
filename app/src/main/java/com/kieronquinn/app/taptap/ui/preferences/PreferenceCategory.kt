package com.kieronquinn.app.taptap.ui.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceViewHolder
import com.kieronquinn.app.taptap.R

class PreferenceCategory : androidx.preference.PreferenceCategory {

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

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val titleView = holder?.findViewById(android.R.id.title) as TextView
        titleView.typeface = ResourcesCompat.getFont(context, R.font.hkgrotesk)
    }


}