package com.kieronquinn.app.taptap.ui.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
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
    private var root: LinearLayout? = null
    private var tintColor: Int? = null

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
        root = summaryView?.parent?.parent as LinearLayout
        setBackgroundTint(tintColor)
    }

    fun setBackgroundTint(@ColorInt tintColor: Int?){
        root?.run {
            if(tintColor != null) {
                background = ContextCompat.getDrawable(context, R.drawable.background_preference)
                backgroundTintList = ColorStateList.valueOf(tintColor)
                foreground = ContextCompat.getDrawable(context, R.drawable.foreground_preference_tinted)
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = resources.getDimension(R.dimen.margin_extra_small).toInt()
                    bottomMargin = resources.getDimension(R.dimen.margin_extra_small).toInt()
                }
            }else{
                background = null
                foreground = ContextCompat.getDrawable(context, R.drawable.foreground_preference)
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = 0
                    bottomMargin = 0
                }
            }
            this@Preference.tintColor = tintColor
        }
    }

}