package com.kieronquinn.app.taptap.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.preference.PreferenceViewHolder
import com.kieronquinn.app.taptap.R

class Preference : androidx.preference.Preference {

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

    private var hasClickListener = false

    override fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener?) {
        super.setOnPreferenceClickListener(onPreferenceClickListener)
        hasClickListener = onPreferenceClickListener != null
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val titleView = holder?.findViewById(android.R.id.title) as TextView
        titleView.isSingleLine = false
        titleView.typeface = ResourcesCompat.getFont(context, R.font.hkgrotesk)
        val summaryView = holder.findViewById(android.R.id.summary) as? TextView
        summaryView?.maxLines = Int.MAX_VALUE
        holder.itemView.post {
            if(!hasClickListener) holder.itemView.isClickable = false
        }
    }

}