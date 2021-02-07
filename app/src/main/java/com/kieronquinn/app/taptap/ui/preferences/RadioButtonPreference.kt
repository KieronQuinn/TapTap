package com.kieronquinn.app.taptap.ui.preferences

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.button.MaterialButton
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.dip

class RadioButtonPreference : CheckBoxPreference {

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        widgetLayoutResource = R.layout.preference_widget_radiobutton
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        widgetLayoutResource = R.layout.preference_widget_radiobutton
    }

    constructor(context: Context?) : this(context, null) {}

    private val buttons = ArrayList<MaterialButton>()

    var buttonsOnNewLine = false

    override fun onClick() {
        if (this.isChecked) {
            return
        }
        super.onClick()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val titleView = holder?.findViewById(android.R.id.title) as TextView
        titleView.isSingleLine = false
        titleView.typeface = ResourcesCompat.getFont(context, R.font.hkgrotesk)
        val summaryView = holder.findViewById(android.R.id.summary) as? TextView
        summaryView?.maxLines = Int.MAX_VALUE
        containerView = titleView.parent as RelativeLayout
        containerView?.layoutParams.apply {
            this as LinearLayout.LayoutParams
            topMargin = 0
            bottomMargin = 0
            leftMargin = 0
            rightMargin = 0
        }
        containerView?.setPadding(0, context.dip(6), context.dip(6), context.dip(12))
        containerView?.clipToPadding = false
        clearButtons()
        addButtonsInternal()
        containerView?.invalidate()
        root = summaryView?.parent?.parent as LinearLayout
        setBackgroundTint(null)
    }

    fun clearButtons() {
        containerView?.removeViewOptional(containerView?.findViewById<MaterialButton>(android.R.id.button1))
        containerView?.removeViewOptional(containerView?.findViewById<MaterialButton>(android.R.id.button2))
        containerView?.removeViewOptional(containerView?.findViewById<MaterialButton>(android.R.id.button3))
    }

    fun clearButtonsList(){
        buttons.clear()
    }

    private fun RelativeLayout.removeViewOptional(view: View?) {
        if (view != null) removeView(view)
    }

    fun addButton(title: String, @DrawableRes icon: Int?, @ColorRes backgroundColor: Int = R.color.colorAccent, callback: () -> Unit) {
        val materialButton = MaterialButton(context)
        materialButton.text = title
        materialButton.setBackgroundColor(context.getColor(backgroundColor))
        materialButton.typeface = Typeface.create(ResourcesCompat.getFont(context, R.font.hkgrotesk), Typeface.BOLD)
        materialButton.isAllCaps = false
        icon?.let {
            materialButton.icon = context.getDrawable(it)
            materialButton.iconGravity = MaterialButton.ICON_GRAVITY_START
        }
        materialButton.setOnClickListener {
            callback.invoke()
        }
        buttons.add(materialButton)
    }

    private var containerView: RelativeLayout? = null
    private fun addButtonsInternal() {
        buttons.forEachIndexed { index, materialButton ->
            val id = when (index) {
                0 -> android.R.id.button1
                1 -> android.R.id.button2
                2 -> android.R.id.button3
                else -> throw Exception("Only 3 buttons are supported")
            }
            materialButton.id = id
            containerView?.addView(applyButton(materialButton) {
                if (buttonsOnNewLine) {
                    when (index) {
                        0 -> {
                            //Just below summary
                            it.addRule(RelativeLayout.BELOW, android.R.id.summary)
                            it.marginStart = context.dip(8)
                            it.topMargin = context.dip(8)
                        }
                        1 -> {
                            //Below button1
                            it.addRule(RelativeLayout.BELOW, android.R.id.button1)
                            it.marginStart = context.dip(8)
                        }
                        2 -> {
                            //Below button2
                            it.addRule(RelativeLayout.BELOW, android.R.id.button2)
                            it.marginStart = context.dip(8)
                        }
                    }
                } else {
                    when (index) {
                        0 -> {
                            //Just below summary
                            it.addRule(RelativeLayout.BELOW, android.R.id.summary)
                            it.topMargin = context.dip(8)
                            it.marginStart = context.dip(8)
                        }
                        1 -> {
                            //Below summary, to right of button1
                            it.addRule(RelativeLayout.BELOW, android.R.id.summary)
                            it.addRule(RelativeLayout.RIGHT_OF, android.R.id.button1)
                            it.topMargin = context.dip(8)
                            it.marginStart = context.dip(8)
                        }
                        2 -> {
                            //Below button1
                            it.addRule(RelativeLayout.BELOW, android.R.id.button1)
                            it.marginStart = context.dip(8)
                        }
                    }
                }
                it
            })
        }
    }

    private fun applyButton(
        materialButton: MaterialButton,
        paramsCallback: (RelativeLayout.LayoutParams) -> RelativeLayout.LayoutParams
    ): MaterialButton {
        materialButton.post {
            materialButton.layoutParams.apply {
                materialButton.layoutParams = paramsCallback.invoke(this as RelativeLayout.LayoutParams)
            }
        }
        materialButton.removeView()
        return materialButton
    }

    fun getButton(position: Int) : MaterialButton? {
        val id = when(position){
            0 -> android.R.id.button1
            1 -> android.R.id.button2
            2 -> android.R.id.button3
            else -> throw Exception("Attempting to get non-existent button")
        }
        return containerView?.findViewById(id)
    }

    private fun View.removeView(){
        (this.parent as? ViewGroup)?.removeView(this)
    }

    private lateinit var root: LinearLayout
    fun setBackgroundTint(@ColorInt tintColor: Int?){
        root.run {
            if(tintColor != null) {
                background = ContextCompat.getDrawable(context, R.drawable.background_preference)
                backgroundTintList = ColorStateList.valueOf(tintColor)
                foreground = ContextCompat.getDrawable(context, R.drawable.foreground_preference_tinted)
            }else{
                background = null
                foreground = ContextCompat.getDrawable(context, R.drawable.foreground_preference)
            }
        }
    }

}