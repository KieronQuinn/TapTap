package com.kieronquinn.app.taptap.ui.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.chip.Chip
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.setAccessibleR
import java.lang.Integer.min

class ChipPreference : Preference {

    init {
        layoutResource = R.layout.preference_chip
    }

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
    private var root: View? = null
    private var tintColor: Int? = null
    private var chips: Array<PreferenceChip>? = null
    private var holder: PreferenceViewHolder? = null

    override fun setOnPreferenceClickListener(onPreferenceClickListener: OnPreferenceClickListener?) {
        super.setOnPreferenceClickListener(onPreferenceClickListener)
        hasClickListener = onPreferenceClickListener != null
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val titleView = holder.findViewById(android.R.id.title) as TextView
        titleView.isSingleLine = false
        titleView.typeface = ResourcesCompat.getFont(context, R.font.hkgrotesk)
        val summaryView = holder.findViewById(android.R.id.summary) as? TextView
        summaryView?.maxLines = Int.MAX_VALUE
        holder.itemView.post {
            if(!hasClickListener) holder.itemView.isClickable = false
        }
        root = holder.findViewById(R.id.preference_container)
        val itemView = holder.itemView
        itemView.setOnClickListener(null)
        root?.setOnClickListener(getOnClickListener())
        setBackgroundTint(tintColor)
        setupChips(holder)
        this.holder = holder
        ViewCompat.setBackground(itemView, null)
    }

    override fun onDetached() {
        super.onDetached()
        holder = null
    }

    fun getOnClickListener(): View.OnClickListener? {
        return Preference::class.java.getDeclaredField("mClickListener").setAccessibleR(true).get(this) as? View.OnClickListener
    }

    fun setBackgroundTint(@ColorInt tintColor: Int?){
        root?.run {
            if(tintColor != null) {
                background = ContextCompat.getDrawable(context, R.drawable.background_preference)
                backgroundTintList = ColorStateList.valueOf(tintColor)
                foreground = ContextCompat.getDrawable(context, R.drawable.foreground_preference_tinted)
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = resources.getDimension(R.dimen.margin_extra_small).toInt()
                    bottomMargin = if(chips?.isNotEmpty() == true) 0 else resources.getDimension(R.dimen.margin_extra_small).toInt()
                }
            }else{
                background = null
                foreground = ContextCompat.getDrawable(context, R.drawable.foreground_preference)
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = 0
                    bottomMargin = 0
                }
            }
            this@ChipPreference.tintColor = tintColor
        }
    }

    private fun setupChips(holder: PreferenceViewHolder){
        val size = chips?.size ?: 0
        val chip1 = holder.findViewById(R.id.preference_chip_1) as Chip
        val chip2 = holder.findViewById(R.id.preference_chip_2) as Chip
        val chip3 = holder.findViewById(R.id.preference_chip_3) as Chip
        chip1.isVisible = size > 0
        chip2.isVisible = size > 1
        chip3.isVisible = size > 2
        val chips = arrayOf(chip1, chip2, chip3)
        for(i in 0 until min(size, chips.size)){
            val chip = chips[i]
            val chipData = this.chips!![i]
            chip.run {
                setChipBackgroundColorResource(chipData.background)
                setText(chipData.title)
                setChipIconResource(chipData.icon)
                setOnClickListener {
                    chipData.clickListener.invoke()
                }
            }
        }
    }

    fun setChips(chips: Array<PreferenceChip>){
        this.chips = chips
        holder?.let {
            setupChips(it)
        }
    }

    data class PreferenceChip(@ColorRes val background: Int, @StringRes val title: Int, @DrawableRes val icon: Int, val clickListener: () -> Unit)

}