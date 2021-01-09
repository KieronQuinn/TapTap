package com.kieronquinn.app.taptap.v2.ui.screens.setup.battery

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
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.navigation.fragment.findNavController
import com.judemanutd.autostarter.AutoStartPermissionHelper
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.ModalActivity
import com.kieronquinn.app.taptap.core.TapSharedPreferences.Companion.SHARED_PREFERENCES_KEY_HAS_SEEN_SETUP
import com.kieronquinn.app.taptap.databinding.FragmentSetupBatteryBinding
import com.kieronquinn.app.taptap.utils.isMiui
import com.kieronquinn.app.taptap.utils.observe
import com.kieronquinn.app.taptap.utils.resolveColorAttribute
import com.kieronquinn.app.taptap.utils.sharedPreferences
import com.kieronquinn.app.taptap.v2.components.base.BoundFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_setup_battery.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.lang.Exception
import kotlin.math.roundToInt

class SetupBatteryFragment: BoundFragment<FragmentSetupBatteryBinding>(FragmentSetupBatteryBinding::class.java) {

    private val viewModel by viewModel<SetupBatteryViewModel>()

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            toolbar.run {
                applySystemWindowInsetsToMargin(top = true)
                setNavigationIcon(R.drawable.ic_back)
                setNavigationOnClickListener {
                    onBackPressed()
                }
            }
            configurationNext.setOnApplyWindowInsetsListener { view, windowInsets ->
                view.updateLayoutParams<FrameLayout.LayoutParams> {
                    height = (buttonHeight + windowInsets.systemWindowInsetBottom).roundToInt()
                }
                view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
                windowInsets
            }
            configurationNext.setOnClickListener {
                viewModel.onNextClicked(this@SetupBatteryFragment)
            }
            setupBatteryButton.setOnClickListener {
                viewModel.onBatteryButtonClicked(it.context)
            }
            setupBatteryButtonMiui.setOnClickListener {
                viewModel.onMiuiBatteryButtonClicked(it.context)
            }
            setupBatteryButtonDontKill.setOnClickListener {
                viewModel.onDontKillButtonClicked(it.context)
            }
            setupBatteryButtonOem.setOnClickListener {
                viewModel.onOemBatteryButtonClicked(it.context)
            }
            avd.applySystemWindowInsetsToMargin(top = true)
            if(activity is ModalActivity){
                //Hide bottom nav
                configurationNavigationContainer.visibility = View.GONE
            }
        }
        with(viewModel){
            batteryOptimisationDisabled.observe(viewLifecycleOwner){
                binding.setupBatteryButton.isEnabled = !it
                binding.configurationNext.run {
                    isEnabled = it
                    val textColor = if(it) ContextCompat.getColor(context, R.color.colorAccent) else ColorUtils.setAlphaComponent(ContextCompat.getColor(context, context.resolveColorAttribute(android.R.attr.textColorSecondaryNoDisable)), 128)
                    setTextColor(textColor)
                    TextViewCompat.setCompoundDrawableTintList(this, if(it) ColorStateList.valueOf(textColor) else null)
                }
                binding.setupBatteryButton.run {
                    if(it){
                        text = getString(R.string.setup_battery_button_disabled)
                        isEnabled = false
                        setIconResource(R.drawable.ic_check)
                    }else{
                        text = getString(R.string.setup_battery_button)
                        isEnabled = true
                        setIconResource(R.drawable.ic_next)
                    }
                }
            }
            shouldShowMiuiButton.observe(viewLifecycleOwner){
                binding.setupBatteryButtonMiui.isVisible = it
            }
            shouldShowOemButton.observe(viewLifecycleOwner){
                binding.setupBatteryButtonOem.isVisible = it
            }
            shouldShowDontKillButton.observe(viewLifecycleOwner){
                binding.setupBatteryButtonDontKill.isVisible = it
            }
            miuiOptimisationDisabled.observe(viewLifecycleOwner){
                binding.setupBatteryButtonMiui.run {
                    if(it){
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
            checkMiuiOptimisation(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        with(binding){
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
        }
        with(viewModel){
            checkBatteryOptimisation(requireContext())
            checkOem(requireContext())
        }
    }

    override fun onBackPressed(): Boolean = viewModel.onBackPressed(this)

}