package com.kieronquinn.app.taptap.ui.screens.setup.foss

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.updateLayoutParams
import com.airbnb.lottie.LottieDrawable
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSetupFossInfoBinding
import com.kieronquinn.app.taptap.components.base.BoundFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.koin.android.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class SetupFossInfoFragment: BoundFragment<FragmentSetupFossInfoBinding>(FragmentSetupFossInfoBinding::class.java) {

    private val viewModel by viewModel<SetupFossInfoViewModel>()

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            setupFossInfoContent.apply {
                val spannable = HtmlCompat.fromHtml(
                    getString(R.string.setup_foss_info_content),
                    HtmlCompat.FROM_HTML_MODE_COMPACT
                )
                text = spannable
                BetterLinkMovementMethod.linkifyHtml(this)
                    .setOnLinkClickListener { textView, urlText ->
                        viewModel.onLinkClicked(context, urlText)
                        true
                    }
            }

            fossToolbar.run {
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
                view.setPadding(
                    view.paddingLeft,
                    view.paddingTop,
                    view.paddingRight,
                    windowInsets.systemWindowInsetBottom
                )
                windowInsets
            }

            scrollView.applySystemWindowInsetsToPadding(bottom = true)

            configurationNext.setOnClickListener {
                viewModel.onNextClicked(this@SetupFossInfoFragment)
            }

            setupFossInfoWarning.setOnClickListener {
                viewModel.onFossInfoClicked(this@SetupFossInfoFragment)
            }

            lottieAnimation.applySystemWindowInsetsToMargin(top = true)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.lottieAnimation.apply{
            setAnimation(R.raw.foss)
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
    }

    override fun onBackPressed(): Boolean = viewModel.onBackPressed(this)

}