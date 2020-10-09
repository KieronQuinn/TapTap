package com.kieronquinn.app.taptap.fragments.setup

import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.navigation.fragment.findNavController
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.services.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.*
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_setup_accessibility.*
import kotlinx.android.synthetic.main.fragment_setup_accessibility.configuration_next
import kotlinx.android.synthetic.main.fragment_setup_accessibility.toolbar
import kotlinx.android.synthetic.main.fragment_setup_gesture_configuration.*
import java.lang.RuntimeException
import kotlin.math.roundToInt

class AccessibilityFragment: BaseSetupFragment() {

    private var isWaitingForAccessibility = false

    private val returnReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            context?.unregisterReceiverOpt(this)
            isWaitingForAccessibility = false
            try {
                startActivity(requireActivity().intent.apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                })
            }catch (e: RuntimeException){
                //Fragment isn't attached
            }
        }
    }

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup_accessibility, container, false)
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
            findNavController().navigate(R.id.action_accessibilityFragment_to_batteryFragment)
        }

        setup_accessibility_button.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                val bundle = Bundle()
                val componentName = ComponentName(BuildConfig.APPLICATION_ID, TapAccessibilityService::class.java.name).flattenToString()
                bundle.putString(EXTRA_FRAGMENT_ARG_KEY, componentName)
                putExtra(EXTRA_FRAGMENT_ARG_KEY, componentName)
                putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, bundle)
            })
            isWaitingForAccessibility = true
            it.context.registerReceiver(returnReceiver, IntentFilter(TapAccessibilityService.KEY_ACCESSIBILITY_START))
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        avd.applySystemWindowInsetsToMargin(top = true)
    }

    override fun onResume() {
        super.onResume()
        isWaitingForAccessibility = false
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
        val isAccessibilityEnabled = isAccessibilityServiceEnabled(requireContext(), TapAccessibilityService::class.java)
        setup_accessibility_button.run {
            if(isAccessibilityEnabled){
                text = getString(R.string.setup_accessibility_button_enabled)
                isEnabled = false
                setIconResource(R.drawable.ic_check)
            }else{
                text = getString(R.string.setup_accessibility_button)
                isEnabled = true
                setIconResource(R.drawable.ic_next)
            }
        }
        configuration_next.apply {
            isEnabled = isAccessibilityEnabled
            val textColor = if(isAccessibilityEnabled) ContextCompat.getColor(context, R.color.colorAccent) else ColorUtils.setAlphaComponent(ContextCompat.getColor(context, context.resolveColorAttribute(android.R.attr.textColorSecondaryNoDisable)), 128)
            setTextColor(textColor)
            TextViewCompat.setCompoundDrawableTintList(this, if(isAccessibilityEnabled) ColorStateList.valueOf(textColor) else null)
        }
    }

    override fun onPause() {
        super.onPause()
        if(!isWaitingForAccessibility) requireContext().unregisterReceiverOpt(returnReceiver)
    }

    override fun onBackPressed(): Boolean {
        findNavController().navigateUp()
        return true
    }


}