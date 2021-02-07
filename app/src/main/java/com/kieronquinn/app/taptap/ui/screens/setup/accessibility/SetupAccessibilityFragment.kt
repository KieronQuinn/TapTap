package com.kieronquinn.app.taptap.ui.screens.setup.accessibility

import android.content.res.ColorStateList
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSetupAccessibilityBinding
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.utils.extensions.resolveColorAttribute
import com.kieronquinn.app.taptap.components.base.BoundFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class SetupAccessibilityFragment: BoundFragment<FragmentSetupAccessibilityBinding>(FragmentSetupAccessibilityBinding::class.java) {

    private val viewModel by viewModel<SetupAccessibilityViewModel>()

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            accessibilityToolbar.run {
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
                viewModel.onNextClicked(this@SetupAccessibilityFragment)
            }
            setupAccessibilityButton.setOnClickListener {
                viewModel.onAccessibilityClicked(it.context)
            }
            avd.applySystemWindowInsetsToMargin(top = true)
        }
        with(viewModel){
            setupAccessibilityListener(requireContext())
            setupAccessibilityLaunchListener(requireActivity())
            isAccessibilityServiceEnabled.observe(viewLifecycleOwner){ enabled: Boolean ->
                onServiceStateChanged(enabled)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.avd.let { imageView ->
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

    private fun onServiceStateChanged(enabled: Boolean){
        binding.setupAccessibilityButton.run {
            isEnabled = !enabled
            if(enabled){
                text = getString(R.string.setup_accessibility_button_enabled)
                setIconResource(R.drawable.ic_check)
            }else{
                text = getString(R.string.setup_accessibility_button)
                setIconResource(R.drawable.ic_next)
            }
        }
        binding.configurationNext.run {
            isEnabled = enabled
            val textColor = if(enabled) ContextCompat.getColor(context, R.color.colorAccent) else ColorUtils.setAlphaComponent(ContextCompat.getColor(context, context.resolveColorAttribute(android.R.attr.textColorSecondaryNoDisable)), 128)
            setTextColor(textColor)
            TextViewCompat.setCompoundDrawableTintList(this, if(enabled) ColorStateList.valueOf(textColor) else null)
        }
    }

    override fun onBackPressed(): Boolean = viewModel.onBackPressed(this)


}