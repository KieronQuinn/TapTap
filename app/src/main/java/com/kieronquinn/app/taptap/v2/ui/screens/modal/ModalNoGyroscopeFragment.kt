package com.kieronquinn.app.taptap.v2.ui.screens.modal

import android.content.pm.ActivityInfo
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentModalNoGyroscopeBinding
import com.kieronquinn.app.taptap.fragments.BaseFragment
import com.kieronquinn.app.taptap.v2.components.base.BoundFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_modal_no_gyroscope.avd
import kotlinx.android.synthetic.main.fragment_modal_no_gyroscope.configuration_next
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class ModalNoGyroscopeFragment: BoundFragment<FragmentModalNoGyroscopeBinding>(FragmentModalNoGyroscopeBinding::class.java) {

    private val viewModel by viewModel<ModalNoGyroscopeViewModel>()

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        with(binding){
            avd.let { imageView ->
                (imageView.drawable as AnimatedVectorDrawable).let { avd ->
                    avd.registerAnimationCallback(object: Animatable2.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            super.onAnimationEnd(drawable)
                            imageView.postDelayed({
                                avd.start()
                            }, 500)
                        }
                    })
                    avd.start()
                }
                imageView.applySystemWindowInsetsToMargin(top = true)
            }
            configurationNext.setOnClickListener {
                viewModel.onNextClicked(this@ModalNoGyroscopeFragment)
            }
            configurationNext.setOnApplyWindowInsetsListener { view, windowInsets ->
                view.updateLayoutParams<FrameLayout.LayoutParams> {
                    height = (buttonHeight + windowInsets.systemWindowInsetBottom).roundToInt()
                }
                view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
                windowInsets
            }
        }
    }

}