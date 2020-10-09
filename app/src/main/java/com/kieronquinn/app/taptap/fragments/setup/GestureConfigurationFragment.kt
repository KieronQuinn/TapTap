package com.kieronquinn.app.taptap.fragments.setup

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.hardware.SensorEvent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieDrawable.RESTART
import com.android.internal.logging.MetricsLogger
import com.android.systemui.keyguard.WakefulnessLifecycle
import com.google.android.systemui.columbus.ColumbusContentObserver
import com.google.android.systemui.columbus.ContentResolverWrapper
import com.google.android.systemui.columbus.PowerManagerWrapper
import com.google.android.systemui.columbus.sensors.CustomTapRT
import com.google.android.systemui.columbus.sensors.GestureSensor
import com.google.android.systemui.columbus.sensors.GestureSensorImpl
import com.google.android.systemui.columbus.sensors.config.Adjustment
import com.google.android.systemui.columbus.sensors.config.GestureConfiguration
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.activities.SettingsActivity
import com.kieronquinn.app.taptap.columbus.actions.DoNothingAction
import com.kieronquinn.app.taptap.columbus.feedback.HapticClickCompat
import com.kieronquinn.app.taptap.fragments.bottomsheets.MaterialBottomSheetDialogFragment
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.getDefaultTfModel
import com.kieronquinn.app.taptap.services.TapColumbusService
import com.kieronquinn.app.taptap.smaliint.SmaliCalls
import com.kieronquinn.app.taptap.utils.*
import dev.chrisbanes.insetter.applySystemGestureInsetsToPadding
import dev.chrisbanes.insetter.applySystemWindowInsetsToMargin
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.android.synthetic.main.fragment_setup_gesture_configuration.*
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.lang.Integer.max
import kotlin.math.roundToInt

class GestureConfigurationFragment: BaseSetupFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var columbusService: TapColumbusService? = null
    private var gestureSensorImpl: GestureSensorImpl? = null
    private var hasInfoBoxTransitioned = false

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setup_gesture_configuration, container, false)
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
        bottomSheet.applySystemWindowInsetsToPadding(bottom = true)
        configuration_troubleshooting.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.updateLayoutParams<FrameLayout.LayoutParams> {
                height = (buttonHeight + windowInsets.systemWindowInsetBottom).roundToInt()
            }
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
            windowInsets
        }
        configuration_next.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.updateLayoutParams<FrameLayout.LayoutParams> {
                height = (buttonHeight + windowInsets.systemWindowInsetBottom).roundToInt()
            }
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
            windowInsets
        }
        configuration_troubleshooting.setOnClickListener {
            showTroubleshootingBottomSheet()
        }
        setupRippleView()
        configuration_next.setOnClickListener {
            findNavController().navigate(R.id.action_gestureConfigurationFragment_to_FOSSInfoFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        view?.keepScreenOn = true
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        //Disable gesture so we can react without being interfered with
        sharedPreferences?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, false)?.apply()
        setup_gesture_configuration_listening.animate().alpha(1f).setDuration(1000L).start()
        setup_gesture_configuration_video.run {
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
        hasInfoBoxTransitioned = false
        //Start gesture
        startTemporaryTap()
    }

    private fun setupRippleView(){
        rippleView.run {
            setup_gesture_configuration_video.post {
                //Calculate position
                val centerY = setup_gesture_configuration_video.run {
                    val rect = Rect()
                    getGlobalVisibleRect(rect)
                    rect.top + (measuredHeight / 2)
                }
                val bottomSheetPosition = bottomSheet.run {
                    val rect = Rect()
                    getGlobalVisibleRect(rect)
                    rect.top
                }
                val distanceFromBottomSheet = bottomSheetPosition - centerY
                val calculatedHeight = max(distanceFromBottomSheet, centerY) * 2
                updateLayoutParams<FrameLayout.LayoutParams> {
                    height = calculatedHeight
                }
                this.y = centerY - (calculatedHeight.toFloat() / 2)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        setup_gesture_configuration_video.setBackgroundColor(Color.TRANSPARENT)
        setup_gesture_configuration_video.alpha = 0f
        //Wait for video to disappear before moving on, this fixes the black/white box visible in the animation
        setup_gesture_configuration_video.doOnNextLayout {
            findNavController().navigateUp()
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        view?.keepScreenOn = false
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        //Stop tap
        stopTap()
        //Re-enable gesture
        sharedPreferences?.edit()?.putBoolean(SHARED_PREFERENCES_KEY_MAIN_SWITCH, true)?.apply()
    }

    private fun startTemporaryTap(){
        val context = context ?: return

        val activityManagerService = try {
            ActivityManager::class.java.getMethod("getService").invoke(null)
        } catch (e: NoSuchMethodException) {
            val activityManagerNative = Class.forName("android.app.ActivityManagerNative")
            activityManagerNative.getMethod("getDefault").invoke(null)
        }
        val gestureConfiguration = createGestureConfiguration(context, activityManagerService)
        this.gestureSensorImpl = GestureSensorImpl(context, gestureConfiguration).apply {
            (getTapRT() as? CustomTapRT)?.isTripleTapEnabled = true
            sensorEventListener = object: GestureSensorImpl.GestureSensorEventListener(){

                init {
                    GestureSensorImpl.GestureSensorEventListener::class.java.getDeclaredField("this\$0").setAccessibleR(true).set(this, this@apply)
                }

                override fun onSensorChanged(arg14: SensorEvent) {
                    val sensor = arg14.sensor
                    val v14 = this@apply.tap.run {
                        updateData(sensor.type, arg14.values[0], arg14.values[1], arg14.values[2], arg14.timestamp, samplingIntervalNs, isRunningInLowSamplingRate)
                        checkDoubleTapTiming(arg14.timestamp)
                    }
                    if(v14 == 1){
                        val handler = this@apply.handler
                        val timeout = this.onTimeout
                        handler.removeCallbacks(timeout)
                        handler.post {
                            if(listener != null){
                                val detectionProperties = GestureSensor.DetectionProperties(false, true, 1)
                                listener.onGestureProgress(this@apply, 1, detectionProperties)
                            }
                            handler.postDelayed(timeout, GestureSensorImpl.TIMEOUT_MS)
                        }
                    }else if(v14 == 2){
                        val handler = this@apply.handler
                        val timeout = this.onTimeout
                        handler.removeCallbacks(timeout)
                        handler.post {
                            if(listener != null){
                                onDoubleTap()
                            }
                            `reset$vendor__unbundled_google__packages__SystemUIGoogle__android_common__sysuig`()
                        }
                    }else if(v14 == 3){
                        val handler = this@apply.handler
                        val timeout = this.onTimeout
                        handler.removeCallbacks(timeout)
                        handler.post {
                            if(listener != null){
                                onTripleTap()
                            }
                            `reset$vendor__unbundled_google__packages__SystemUIGoogle__android_common__sysuig`()
                        }
                    }
                }

            }
        }
        val powerManagerWrapper = PowerManagerWrapper(context)

        //Set model from prefs
        SmaliCalls.setTapRtModel(
            TfModel.valueOf(
                sharedPreferences?.getString(
                    SHARED_PREFERENCES_KEY_MODEL, TfModel.PIXEL4.name
                ) ?: TfModel.PIXEL4.name
            ).model
        )

        //Create the service
        this.columbusService = TapColumbusService(
            context,
            listOf(DoNothingAction(context, true)),
            listOf(DoNothingAction(context, true)).toMutableList(),
            emptySet(),
            emptySet(),
            gestureSensorImpl!!,
            powerManagerWrapper
        )

        configureTap()
    }

    private fun configureTap() {
        gestureSensorImpl?.getTapRT()?.run {
            val sensitivity = sharedPreferences?.getString(SHARED_PREFERENCES_KEY_SENSITIVITY, "0.05")?.toFloatOrNull() ?: 0.05f
            positivePeakDetector.setMinNoiseTolerate(sensitivity)
        }
    }

    private fun stopTap() {
        gestureSensorImpl?.stopListening()
        columbusService?.stop()
    }

    private fun onDoubleTap() {
        pulseHaptic()
        transitionInfoBox()
        rippleView.startAnimation(2)
    }

    private fun onTripleTap() {
        pulseHaptic()
        transitionInfoBox()
        rippleView.startAnimation(3)
    }

    private fun transitionInfoBox(){
        if(hasInfoBoxTransitioned) return
        hasInfoBoxTransitioned = true
        setup_gesture_configuration_listening.animateBackgroundTint(ContextCompat.getColor(setup_gesture_configuration_listening.context, R.color.accessibility_check_circle))
        setup_gesture_configuration_listening_text.text = getString(R.string.setup_gesture_configuration_listening_found)
        lottieAnimation.fadeOut {}
        lottieCheck.fadeIn {}
        configuration_next.apply {
            isEnabled = true
            val textColor = ContextCompat.getColor(context, R.color.colorAccent)
            setTextColor(textColor)
            compoundDrawableTintList = ColorStateList.valueOf(textColor)
        }

    }

    private fun createGestureConfiguration(context: Context, activityManager: Any): GestureConfiguration {
        val contentResolverWrapper = ContentResolverWrapper(context)
        val factory = ColumbusContentObserver.Factory::class.java.constructors.first()
            .newInstance(contentResolverWrapper, activityManager) as ColumbusContentObserver.Factory
        return GestureConfiguration(context, emptySet<Adjustment>(), factory)
    }

    private fun pulseHaptic(){
        val vibrator: Vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val resolveVibrationEffect: VibrationEffect? = getVibrationEffect(5)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && resolveVibrationEffect != null) {
            vibrator.vibrate(resolveVibrationEffect, HapticClickCompat.SONIFICATION_AUDIO_ATTRIBUTES)
        } else {
            vibrator.vibrate(300)
        }
    }

    override fun onSharedPreferenceChanged(sharedPrefs: SharedPreferences, key: String) {
        if(key == SHARED_PREFERENCES_KEY_SENSITIVITY){
            //Reconfigure tap
            configureTap()
        }
        if (key == SHARED_PREFERENCES_KEY_MODEL) {
            context?.let {
                val default = it.getDefaultTfModel().name
                val model = TfModel.valueOf(sharedPreferences?.getString(SHARED_PREFERENCES_KEY_MODEL, default) ?: default)
                gestureSensorImpl?.setTfClassifier(it.assets, model.model)
            }
        }
    }

    private fun showTroubleshootingBottomSheet(){
        MaterialBottomSheetDialogFragment.create(MaterialBottomSheetDialogFragment(), childFragmentManager, "bs_troubleshooting"){
            it.apply {
                title(R.string.bs_troubleshooting_title)
                message(R.string.bs_troubleshooting_content)
            }
        }
    }



}