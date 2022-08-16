package com.kieronquinn.app.taptap.ui.screens.setup.gesture

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.vectordrawable.graphics.drawable.SeekableAnimatedVectorDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.GestureConfigurationNavigation
import com.kieronquinn.app.taptap.components.navigation.setupWithNavigation
import com.kieronquinn.app.taptap.databinding.FragmentSetupGestureBinding
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.ProvidesBack
import com.kieronquinn.app.taptap.ui.base.ProvidesTitle
import com.kieronquinn.app.taptap.ui.screens.setup.base.BaseSetupFragment
import com.kieronquinn.app.taptap.ui.screens.setup.gesture.SetupGestureViewModel.InfoCard
import com.kieronquinn.app.taptap.ui.screens.setup.gesture.SetupGestureViewModel.State
import com.kieronquinn.app.taptap.ui.views.RippleView
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import kotlinx.coroutines.delay
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class SetupGestureFragment: BaseSetupFragment<FragmentSetupGestureBinding>(FragmentSetupGestureBinding::inflate), ProvidesBack {

    private val configurationNavigation by inject<GestureConfigurationNavigation>()

    //Local state for resuming only
    private var isLoaded = false

    override val viewModel by viewModel<SetupGestureViewModel>()
    override val toolbar by lazy {
        binding.toolbar
    }

    private val maxCardCornerRadius by lazy {
        resources.getDimension(R.dimen.margin_16)
    }

    private val bottomSheetBehavior by lazy {
        BottomSheetBehavior.from(binding.setupGestureBottomSheet)
    }

    private val minToolbarHeight by lazy {
        requireContext().actionBarSize
    }

    private val toolbarHeightDiff by lazy {
        val maxHeight = resources.getDimension(R.dimen.setup_gesture_bottom_sheet_peek_size)
        maxHeight - minToolbarHeight
    }

    private val navHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.fragment_container_gesture_configuration) as NavHostFragment
    }

    private val navController by lazy {
        navHostFragment.navController
    }

    private val closeAvd by lazy {
        SeekableAnimatedVectorDrawable.create(requireContext(), R.drawable.avd_expand_to_close)
    }

    private val closeToBackAvd by lazy {
        SeekableAnimatedVectorDrawable.create(requireContext(), R.drawable.avd_close_to_back)
    }

    private val backToCloseAvd by lazy {
        SeekableAnimatedVectorDrawable.create(requireContext(), R.drawable.avd_back_to_close)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        setupLottie()
        setupMonet()
        setupTaps()
        setupDoubleTaps()
        setupTripleTaps()
        setupInsets()
        setupBottomSheet()
        setupBottomSheetNavBlock()
        setupBottomSheetStatusBlock()
        setupBottomSheetRoundedCorners()
        setupToolbar()
        setupNavigationTitle()
        setupConfigurationNavigation()
        setupStack()
        setupCloseBottomSheet()
        setupOpenBottomSheet()
        setupBottomSheetDraggable()
        setupInfoCard()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        when(state){
            is State.Loading -> {
                binding.setupGestureBottomSheet.isVisible = false
                binding.setupGestureMain.isVisible = false
                binding.setupGestureLoading.isVisible = true
            }
            is State.Loaded -> {
                isLoaded = true
                binding.setupGestureBottomSheet.isVisible = true
                binding.setupGestureMain.isVisible = true
                binding.setupGestureLoading.isVisible = false
                startDemoMode()
            }
        }
    }

    private fun startDemoMode() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        delay(100L)
        viewModel.startDemoMode(requireContext())
    }

    private fun setupMonet() {
        binding.setupGestureMainContainer.enableChangingAnimations()
        binding.setupGestureLoadingProgress.applyMonet()
    }

    private fun setupInsets() {
        binding.toolbar.onApplyInsets { view, insets ->
            view.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
            viewModel.onInsetsChange(insets)
        }
    }

    private fun setupLottie() = with(binding.setupGestureLottie) {
        val lottieRes = when(Build.MODEL){
            "Pixel 6 Pro" -> R.raw.gesture_columbus_circle_p6
            "Pixel 6" -> R.raw.gesture_columbus_circle_p6
            else -> R.raw.gesture_columbus_circle_p5
        }
        setAnimation(lottieRes)
        val accent = monet.getAccentColor(requireContext(), false)
        replaceColour(".blue400", "**", replaceWith = accent)
        playAnimation()
    }

    private fun setupTaps() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.tapEvents.collect {
            binding.setupGestureRipple.addRipple(RippleView.RippleType.SINGLE_TAP)
        }
    }

    private fun setupDoubleTaps() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.doubleTapEvents.collect {
            binding.setupGestureRipple.addRipple(RippleView.RippleType.DOUBLE_TAP)
        }
    }

    private fun setupTripleTaps() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.tripleTapEvents.collect {
            binding.setupGestureRipple.addRipple(RippleView.RippleType.TRIPLE_TAP)
        }
    }

    private fun setupBottomSheet() {
        val toolbarColor = monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(requireContext())
        binding.setupGestureBottomSheet.setCardBackgroundColor(monet.getBackgroundColor(requireContext()))
        binding.setupGestureBottomSheetToolbarBackground.setBackgroundColor(toolbarColor)
        binding.setupGestureBottomSheetNavbarBlock.setBackgroundColor(toolbarColor)
        binding.setupGestureBottomSheetStatusbarBlock.setBackgroundColor(toolbarColor)
        binding.root.onApplyInsets { view, insets ->
            bottomSheetBehavior.peekHeight = resources.getDimension(R.dimen.setup_gesture_bottom_sheet_peek_size).toInt() +
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            bottomSheetBehavior.slideOffset().collect {
                viewModel.onBottomSheetSlideOffsetChange(it)
            }
        }
    }

    private fun setupBottomSheetNavBlock() {
        binding.root.rootWindowInsets?.let {
            setBottomSheetNavBlockHeight(
                WindowInsetsCompat.toWindowInsetsCompat(it)
                    .getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            )
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.bottomSheetNavBarBlockHeight.collect {
                setBottomSheetNavBlockHeight(it)
            }
        }
    }

    private fun setBottomSheetNavBlockHeight(height: Int) {
        binding.setupGestureBottomSheetNavbarBlock.updateLayoutParams<LinearLayout.LayoutParams> {
            this.height = height
        }
    }

    private fun setupBottomSheetStatusBlock() {
        binding.root.rootWindowInsets?.let {
            setBottomSheetStatusBlockHeight(
                WindowInsetsCompat.toWindowInsetsCompat(it)
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top
            )
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.bottomSheetStatusBarBlockHeight.collect {
                setBottomSheetStatusBlockHeight(it)
            }
        }
    }

    private fun setBottomSheetStatusBlockHeight(height: Int) {
        binding.setupGestureBottomSheetStatusbarBlock.updateLayoutParams<LinearLayout.LayoutParams> {
            this.height = height
        }
    }

    private fun setupBottomSheetRoundedCorners() {
        setBottomSheetRoundedCorner(viewModel.bottomSheetRoundedCornerMultiplier.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.bottomSheetRoundedCornerMultiplier.collect {
                setBottomSheetRoundedCorner(it)
            }
        }
    }

    private fun setBottomSheetRoundedCorner(multiplier: Float) {
        binding.setupGestureBottomSheet.shapeAppearanceModel =
            getCardShapeAppearanceOverlay(multiplier * maxCardCornerRadius)
        binding.setupGestureBottomSheetToolbarBackground.shapeAppearanceModel =
            getCardShapeAppearanceOverlay(multiplier * maxCardCornerRadius)
    }

    private fun getCardShapeAppearanceOverlay(radius: Float): ShapeAppearanceModel {
        return ShapeAppearanceModel().toBuilder().apply {
            setTopLeftCorner(CornerFamily.ROUNDED, radius)
            setTopRightCorner(CornerFamily.ROUNDED, radius)
        }.build()
    }

    private fun setupToolbar() {
        setToolbarNavigationIcon(viewModel.toolbarIcon.value)
        closeAvd?.currentPlayTime = viewModel.toolbarIconPlaytime.value
        setToolbarHeightMultiplier(viewModel.toolbarHeightMultiplier.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.toolbarIconPlaytime.collect {
                closeAvd?.currentPlayTime = it
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.setupGestureBottomSheetToolbar.onNavigationIconClicked().collect {
                if(viewModel.bottomSheetExpanded.value) {
                    onBackPressed()
                }else{
                    toggleBottomSheet()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            binding.setupGestureBottomSheetToolbar.onClicked().collect {
                if(!viewModel.bottomSheetDraggable.value) return@collect
                toggleBottomSheet()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.toolbarHeightMultiplier.collect {
                setToolbarHeightMultiplier(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.toolbarIcon.collect {
                setToolbarNavigationIcon(it)
            }
        }
    }

    private fun setToolbarNavigationIcon(icon: SetupGestureViewModel.ToolbarIcon) {
        binding.setupGestureBottomSheetToolbar.navigationIcon = when(icon){
            SetupGestureViewModel.ToolbarIcon.CLOSE -> closeAvd
            SetupGestureViewModel.ToolbarIcon.CLOSE_TO_BACK -> closeToBackAvd.also {
                it?.currentPlayTime = 0L
                it?.start()
            }
            SetupGestureViewModel.ToolbarIcon.BACK_TO_CLOSE -> backToCloseAvd.also {
                it?.currentPlayTime = 0L
                it?.start()
            }
        }
    }

    private fun setToolbarHeightMultiplier(multiplier: Float){
        binding.setupGestureBottomSheetToolbar.updateLayoutParams<FrameLayout.LayoutParams> {
            height = (minToolbarHeight + (toolbarHeightDiff * multiplier)).roundToInt()
        }
    }

    private fun setupCloseBottomSheet() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.bottomSheetCloseBus.collect {
            closeBottomSheet()
        }
    }

    private fun setupOpenBottomSheet() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.bottomSheetOpenBus.collect {
            openBottomSheet()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun toggleBottomSheet() {
        val newState = when(bottomSheetBehavior.lastStableState){
            BottomSheetBehavior.STATE_COLLAPSED -> BottomSheetBehavior.STATE_EXPANDED
            BottomSheetBehavior.STATE_EXPANDED -> BottomSheetBehavior.STATE_COLLAPSED
            else -> return
        }
        bottomSheetBehavior.state = newState
    }

    private fun closeBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun openBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun setupConfigurationNavigation() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        navHostFragment.setupWithNavigation(configurationNavigation)
    }

    private fun setupNavigationTitle() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        navController.onDestinationChanged().collect {
            val label = it.label
            if(label == null || label.isBlank()) return@collect
            binding.setupGestureBottomSheetToolbar.title = label
        }
    }

    private fun setupStack() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        navHostFragment.childBackStackTopFragment().collect {
            onTopFragmentChanged(it ?: return@collect)
        }
    }

    private fun setupBottomSheetDraggable() {
        bottomSheetBehavior.isDraggable = viewModel.bottomSheetDraggable.value
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.bottomSheetDraggable.collect {
                bottomSheetBehavior.isDraggable = it
            }
        }
    }

    private fun setupInfoCard() {
        handleInfoCard(viewModel.infoCard.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.infoCard.collect {
                handleInfoCard(it)
            }
        }
    }

    private fun handleInfoCard(infoCard: InfoCard) {
        binding.setupGestureInfo.setText(infoCard.contentRes)
        binding.itemSettingsInfoIcon.setImageResource(infoCard.icon)
        val fallbackBackground =
            if (requireContext().isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
        binding.setupGestureInfoCard.setCardBackgroundColor(
            when(infoCard.cardColor){
                InfoCard.CardColor.PRIMARY -> monet.getPrimaryColor(requireContext())
                InfoCard.CardColor.BACKGROUND_SECONDARY -> monet.getBackgroundColorSecondary(requireContext())
                    ?: ContextCompat.getColor(requireContext(), fallbackBackground)
            }
        )
        when(infoCard){
            InfoCard.HINT, InfoCard.HELP -> {
                binding.setupGestureInfoCard.removeRippleForeground()
                binding.setupGestureInfoCard.setOnClickListener(null)
            }
            InfoCard.SUCCESS -> {
                binding.setupGestureInfoCard.addRippleForeground()
                viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                    binding.setupGestureInfoCard.onClicked().collect {
                        viewModel.onNextClicked()
                    }
                }
            }
        }
    }

    private fun onTopFragmentChanged(topFragment: Fragment) {
        viewModel.setConfigurationBackAvailable(topFragment is BackAvailable)
        (topFragment as? ProvidesTitle)?.let {
            val label = it.getTitle()
            if(label == null || label.isBlank()) return@let
            binding.setupGestureBottomSheetToolbar.title = label
        }
    }

    override fun onResume() {
        super.onResume()
        if(isLoaded) {
            startDemoMode()
        }
    }

    override fun onPause() {
        viewModel.stopDemoMode(requireContext())
        super.onPause()
    }

    override fun onBackPressed() = viewModel.onBackPressed()

}