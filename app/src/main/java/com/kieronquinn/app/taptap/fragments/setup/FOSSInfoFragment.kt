package com.kieronquinn.app.taptap.fragments.setup

import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieFrameInfo
import com.airbnb.lottie.value.SimpleLottieValueCallback
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.fragments.bottomsheets.MaterialBottomSheetDialogFragment
import com.kieronquinn.app.taptap.services.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.isAccessibilityServiceEnabled
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import kotlinx.android.synthetic.main.fragment_setup_foss_info.*
import kotlinx.android.synthetic.main.fragment_setup_foss_info.lottieAnimation
import kotlinx.android.synthetic.main.fragment_setup_foss_info.toolbar
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import kotlin.math.roundToInt

class FOSSInfoFragment: BaseSetupFragment() {

    private val buttonHeight by lazy {
        resources.getDimension(R.dimen.configuration_button_height)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup_foss_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup_foss_info_content.apply {
            val spannable = HtmlCompat.fromHtml(getString(R.string.setup_foss_info_content), HtmlCompat.FROM_HTML_MODE_COMPACT)
            text = spannable
            BetterLinkMovementMethod.linkifyHtml(this).setOnLinkClickListener { textView, urlText ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(urlText)
                startActivity(intent)
                true
            }
        }
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
            if(isAccessibilityServiceEnabled(requireContext(), TapAccessibilityService::class.java)){
                findNavController().navigate(R.id.action_FOSSInfoFragment_to_batteryFragment)
            }else {
                findNavController().navigate(R.id.action_FOSSInfoFragment_to_accessibilityFragment)
            }
        }

        setup_foss_info_warning.setOnClickListener {
            showInfoBottomSheet()
        }

        lottieAnimation.applySystemWindowInsetsToMargin(top = true)
    }

    override fun onResume() {
        super.onResume()
        lottieAnimation.apply{
            setAnimation(R.raw.foss)
            repeatCount = LottieDrawable.INFINITE
            playAnimation()
        }
    }

    private fun showInfoBottomSheet(){
        MaterialBottomSheetDialogFragment.create(MaterialBottomSheetDialogFragment(), childFragmentManager, "bs_foss_info"){
            it.apply {
                title(R.string.setup_foss_info_bs_title)
                message(R.string.setup_foss_info_bs_content)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        findNavController().navigateUp()
        return true
    }


}