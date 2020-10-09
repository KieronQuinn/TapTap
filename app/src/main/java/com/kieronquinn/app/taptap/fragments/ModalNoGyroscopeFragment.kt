package com.kieronquinn.app.taptap.fragments

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
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_modal_no_gyroscope.avd
import kotlinx.android.synthetic.main.fragment_modal_no_gyroscope.configuration_next
import kotlin.math.roundToInt

class ModalNoGyroscopeFragment: BaseFragment() {

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_modal_no_gyroscope, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
        configuration_next.setOnClickListener {
            activity?.finish()
        }

        configuration_next.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.updateLayoutParams<FrameLayout.LayoutParams> {
                height = (buttonHeight + windowInsets.systemWindowInsetBottom).roundToInt()
            }
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
            windowInsets
        }
    }

}