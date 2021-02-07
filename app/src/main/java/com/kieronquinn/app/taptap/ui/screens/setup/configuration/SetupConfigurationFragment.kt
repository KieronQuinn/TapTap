package com.kieronquinn.app.taptap.ui.screens.setup.configuration

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieDrawable
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.core.columbus.actions.getRippleCount
import com.kieronquinn.app.taptap.databinding.FragmentSetupGestureConfigurationBinding
import com.kieronquinn.app.taptap.utils.extensions.animateBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.fadeIn
import com.kieronquinn.app.taptap.utils.extensions.fadeOut
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.components.base.BoundFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class SetupConfigurationFragment: BoundFragment<FragmentSetupGestureConfigurationBinding>(FragmentSetupGestureConfigurationBinding::class.java) {

    private val viewModel by viewModel<SetupConfigurationViewModel>()

    private val videoResource: Int
        get() {
            return if(context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
                R.raw.taptap_double_tap_dark
            }else{
                R.raw.taptap_double_tap
            }
        }

    private val lottieResource: Int
        get() {
            return if(context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES){
                R.raw.waiting_dark
            }else{
                R.raw.waiting
            }
        }

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.configurationToolbar){
            applySystemWindowInsetsToMargin(top = true)
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                onBackPressed()
            }
        }
        with(binding){
            bottomSheet.applySystemWindowInsetsToPadding(bottom = true)
            configurationTroubleshooting.setOnApplyWindowInsetsListener { view, windowInsets ->
                view.updateLayoutParams<FrameLayout.LayoutParams> {
                    height = (buttonHeight + windowInsets.systemWindowInsetBottom).roundToInt()
                }
                view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
                windowInsets
            }
            configurationNext.setOnApplyWindowInsetsListener { view, windowInsets ->
                view.updateLayoutParams<FrameLayout.LayoutParams> {
                    height = (buttonHeight + windowInsets.systemWindowInsetBottom).roundToInt()
                }
                view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
                windowInsets
            }
            configurationTroubleshooting.setOnClickListener {
                viewModel.onTroubleshootingClicked(this@SetupConfigurationFragment)
            }
            configurationNext.setOnClickListener {
                viewModel.onNextClicked(this@SetupConfigurationFragment)
            }
            setupRippleView()
        }
        with(viewModel){
            tapEvent.observe(viewLifecycleOwner){
                binding.rippleView.startAnimation(it.getRippleCount())
            }
            infoboxTransitioned.observe(viewLifecycleOwner){
                transitionInfoBox()
            }
        }
    }

    private fun setupRippleView() {
        binding.rippleView.run {
            binding.setupGestureConfigurationVideo.post {
                //Calculate position
                val centerY = binding.setupGestureConfigurationVideo.run {
                    val rect = Rect()
                    getGlobalVisibleRect(rect)
                    rect.top + (measuredHeight / 2)
                }
                val bottomSheetPosition = binding.bottomSheet.run {
                    val rect = Rect()
                    getGlobalVisibleRect(rect)
                    rect.top
                }
                val distanceFromBottomSheet = bottomSheetPosition - centerY
                val calculatedHeight = Integer.max(distanceFromBottomSheet, centerY) * 2
                updateLayoutParams<FrameLayout.LayoutParams> {
                    height = calculatedHeight
                }
                this.y = centerY - (calculatedHeight.toFloat() / 2)
            }
        }
    }

    private fun transitionInfoBox() = with(binding){
        setupGestureConfigurationListening.animateBackgroundTint(ContextCompat.getColor(setupGestureConfigurationListening.context, R.color.accessibility_check_circle))
        setupGestureConfigurationListeningText.text = getString(R.string.setup_gesture_configuration_listening_found)
        lottieAnimation.fadeOut {}
        lottieCheck.fadeIn {}
        configurationNext.apply {
            isEnabled = true
            val textColor = ContextCompat.getColor(context, R.color.colorAccent)
            setTextColor(textColor)
            compoundDrawableTintList = ColorStateList.valueOf(textColor)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setup(requireContext())
        with(binding){
            setupGestureConfigurationListening.animate().alpha(1f).setDuration(1000L).start()
            setupGestureConfigurationVideo.run {
                setBackgroundResource(videoResource)
            }
            bottomSheet.apply {
                val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom).apply {
                    interpolator = AccelerateDecelerateInterpolator()
                }
                startAnimation(animation)
                alpha = 1f
            }
            lottieAnimation.run {
                setAnimation(lottieResource)
                repeatCount = LottieDrawable.INFINITE
                playAnimation()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.reset()
    }

    override fun onBackPressed(): Boolean {
        return viewModel.onBackPressed(this)
    }

}