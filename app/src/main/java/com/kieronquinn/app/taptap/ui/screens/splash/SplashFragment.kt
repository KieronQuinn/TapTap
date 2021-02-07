package com.kieronquinn.app.taptap.ui.screens.splash

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.transition.Fade
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSplashBinding
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SplashFragment : BoundFragment<FragmentSplashBinding>(FragmentSplashBinding::class.java){

    init {
        exitTransition = Fade()
    }

    private val viewModel by sharedViewModel<ContainerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.lottieAnimation) {
            addValueCallback(KeyPath("**"), LottieProperty.COLOR_FILTER, {
                PorterDuffColorFilter(
                    ContextCompat.getColor(view.context, R.color.colorLottie),
                    PorterDuff.Mode.SRC_ATOP
                )
            })
            addAnimatorUpdateListener {
                //Fade over the end of the animation
                if (it.animatedFraction >= 0.9) {
                    viewModel.notifySplashFinished()
                }
            }
            playAnimation()
        }
    }

}