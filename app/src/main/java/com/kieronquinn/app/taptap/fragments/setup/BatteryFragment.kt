package com.kieronquinn.app.taptap.fragments.setup

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.navigation.fragment.findNavController
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.ModalActivity
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP
import com.kieronquinn.app.taptap.utils.isMiui
import com.kieronquinn.app.taptap.utils.resolveColorAttribute
import com.kieronquinn.app.taptap.utils.sharedPreferences
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_setup_battery.*
import java.lang.Exception
import kotlin.math.roundToInt

class BatteryFragment: BaseSetupFragment() {

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup_battery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.run {
            applySystemWindowInsetsToMargin(top = true)
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }

        configuration_next.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.updateLayoutParams<FrameLayout.LayoutParams> {
                height = (buttonHeight + windowInsets.systemWindowInsetBottom).roundToInt()
            }
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
            windowInsets
        }

        configuration_next.setOnClickListener {
            if(activity is ModalActivity) return@setOnClickListener
            sharedPreferences?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP, true)?.apply()
            findNavController().navigate(R.id.action_batteryFragment_to_settingsActivity)
            activity?.finish()
        }

        setup_battery_button.setOnClickListener {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            }
            startActivity(intent)
        }

        val batteryIntent = Intent("android.settings.APP_BATTERY_SETTINGS").apply {
            data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        }

        if(isMiui){
            setup_battery_button_miui.run {
                visibility = View.VISIBLE
                setOnClickListener {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                    Toast.makeText(context, getString(R.string.setup_battery_button_miui_toast), Toast.LENGTH_LONG).show()
                }
            }
        }

        if(requireContext().packageManager.resolveActivity(batteryIntent, 0) != null) {
            setup_battery_button_oem.visibility = View.VISIBLE
            setup_battery_oem_info.visibility = View.VISIBLE
            setup_battery_button_oem.setOnClickListener {
                startActivity(batteryIntent)
            }
        }else{
            if(AutoStartPermissionHelper.getInstance().isAutoStartPermissionAvailable(requireContext())){
                setup_battery_button_oem.visibility = View.VISIBLE
                setup_battery_oem_info.visibility = View.VISIBLE
                setup_battery_button_oem.setOnClickListener {
                    try {
                        AutoStartPermissionHelper.getInstance()
                            .isAutoStartPermissionAvailable(it.context)
                    }catch (e: Exception){
                        Toast.makeText(it.context, getString(R.string.setup_battery_button_oem_toast), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        avd.applySystemWindowInsetsToMargin(top = true)

        if(activity is ModalActivity){
            //Hide bottom nav
            configuration_navigation_container.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        avd.let { imageView ->
            (imageView.drawable as AnimatedVectorDrawable).let { avd ->
                avd.registerAnimationCallback(object: Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        super.onAnimationEnd(drawable)
                        imageView.postDelayed({
                            avd.start()
                        }, 2500)
                    }
                })
                avd.start()
            }
        }
        val powerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isOptimisationDisabled = powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
        setup_battery_button.run {
            if(isOptimisationDisabled){
                text = getString(R.string.setup_battery_button_disabled)
                isEnabled = false
                setIconResource(R.drawable.ic_check)
            }else{
                text = getString(R.string.setup_battery_button)
                isEnabled = true
                setIconResource(R.drawable.ic_next)
            }
        }
        configuration_next.apply {
            isEnabled = isOptimisationDisabled
            val textColor = if(isOptimisationDisabled) ContextCompat.getColor(context, R.color.colorAccent) else ColorUtils.setAlphaComponent(ContextCompat.getColor(context, context.resolveColorAttribute(android.R.attr.textColorSecondaryNoDisable)), 128)
            setTextColor(textColor)
            TextViewCompat.setCompoundDrawableTintList(this, if(isOptimisationDisabled) ColorStateList.valueOf(textColor) else null)
        }
        if(isMiui){
            setup_battery_button_miui.run {
                if(!context.isMiuiOptimisationEnabled()){
                    text = getString(R.string.setup_battery_button_miui_disabled)
                    isEnabled = false
                    setIconResource(R.drawable.ic_check)
                }else{
                    text = getString(R.string.setup_battery_button_miui)
                    isEnabled = true
                    setIconResource(R.drawable.ic_next)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if(!findNavController().navigateUp()){
            activity?.finish()
        }
        return true
    }

    private fun Context.isMiuiOptimisationEnabled(): Boolean {
        return isMiui && Settings.Secure.getInt(contentResolver, "miui_optimization", 1) == 1
    }


}