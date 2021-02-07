package com.kieronquinn.app.taptap.ui.screens.settings.about

import android.animation.Animator
import android.animation.AnimatorInflater
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.google.android.material.appbar.AppBarLayout
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsAboutBinding
import com.kieronquinn.app.taptap.utils.AppBarStateChangeListener
import com.kieronquinn.app.taptap.components.base.BoundFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsAboutFragment: BoundFragment<FragmentSettingsAboutBinding>(FragmentSettingsAboutBinding::class.java) {

    private val viewModel by viewModel<SettingsAboutViewModel>()

    private val outlineProviderElevation by lazy {
        AnimatorInflater.loadStateListAnimator(requireContext(), R.animator.appbar_always_elevated)
    }

    private val outlineProviderNoElevation by lazy {
        AnimatorInflater.loadStateListAnimator(requireContext(), R.animator.appbar_never_elevated)
    }

    private val appBarStateListener = object: AppBarStateChangeListener(){
        override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
            appBarLayout.stateListAnimator = if(state == State.COLLAPSED) outlineProviderElevation else outlineProviderNoElevation
        }
    }

    private val lottieAnimatorListener = object: Animator.AnimatorListener {
        override fun onAnimationEnd(animation: Animator?) {
            viewLifecycleOwner.lifecycleScope.launch {
                delay(2000L)
                binding.lottie.playAnimation()
            }
        }

        override fun onAnimationRepeat(animation: Animator?) {}

        override fun onAnimationCancel(animation: Animator?) {}

        override fun onAnimationStart(animation: Animator?) {}

    }

    override val disableToolbarBackground: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            collapsingToolbarAppbar.applySystemWindowInsetsToPadding(top = true)
            lottie.addValueCallback(KeyPath("**"), LottieProperty.COLOR_FILTER, {
                PorterDuffColorFilter(
                    ContextCompat.getColor(view.context, R.color.colorLottie),
                    PorterDuff.Mode.SRC_ATOP
                )
            })
            recyclerView.run {
                layoutManager = LinearLayoutManager(context)
                adapter = SettingsAboutAdapter(context, viewModel, viewModel.getItems(requireContext()))
                applySystemWindowInsetsToPadding(bottom = true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        bindToNothing()
        binding.collapsingToolbarAppbar.addOnOffsetChangedListener(appBarStateListener)
        binding.lottie.addAnimatorListener(lottieAnimatorListener)
        binding.lottie.playAnimation()
    }

    override fun onPause() {
        super.onPause()
        binding.collapsingToolbarAppbar.removeOnOffsetChangedListener(appBarStateListener)
        binding.lottie.removeAnimatorListener(lottieAnimatorListener)
        binding.lottie.pauseAnimation()
    }

    override fun onBackPressed() = viewModel.onBackPressed(this)
}