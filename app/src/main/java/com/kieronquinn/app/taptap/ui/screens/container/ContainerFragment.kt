package com.kieronquinn.app.taptap.ui.screens.container

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.MenuInflater
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.components.navigation.setupWithNavigation
import com.kieronquinn.app.taptap.databinding.FragmentContainerBinding
import com.kieronquinn.app.taptap.ui.base.*
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel.FabState.Hidden
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel.FabState.Shown
import com.kieronquinn.app.taptap.utils.extensions.*
import com.kieronquinn.monetcompat.extensions.applyMonet
import com.kieronquinn.monetcompat.extensions.toArgb
import com.kieronquinn.monetcompat.extensions.views.setTint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ContainerFragment: BoundFragment<FragmentContainerBinding>(FragmentContainerBinding::inflate) {

    private val googleSansMedium by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.google_sans_text_medium)
    }

    private val navHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.fragment_container_container) as NavHostFragment
    }

    private val navController by lazy {
        navHostFragment.navController
    }

    private val googleSansTextMedium by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.google_sans_text_medium)
    }

    private val updateSnackbar by lazy {
        Snackbar.make(binding.root, getString(R.string.snackbar_update), Snackbar.LENGTH_INDEFINITE).apply {
            applyMonet()
            setTypeface(googleSansTextMedium)
            anchorView = binding.fragmentContainerBottomFabContainer
            isAnchorViewLayoutListenerEnabled = true
            (view.background as? GradientDrawable)?.cornerRadius = resources.getDimension(R.dimen.snackbar_corner_radius)
            setAction(R.string.snackbar_update_button){
                viewModel.onUpdateClicked()
            }
            onSwipeDismissed {
                viewModel.onUpdateDismissed()
            }
        }
    }

    private val navigation by inject<ContainerNavigation>()
    private val viewModel by viewModel<ContainerViewModel>()
    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonet()
        setupBack()
        setupCollapsingToolbar()
        setupToolbar()
        setupNavigation()
        setupStack()
        setupBottomNavigation()
        setupFabState()
        setupCollapsedState()
        setupSnackbar()
        setupUpdateSnackbar()
        setupColumbusSettingPhoenix()
        viewModel.writeSettingsVersion()
    }

    private fun setupMonet() {
        binding.root.setBackgroundColor(monet.getBackgroundColor(requireContext()))
    }

    private fun setupColumbusSettingPhoenix() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        sharedViewModel.columbusSettingPhoenixBus.collect {
            viewModel.phoenix()
        }
    }

    private fun setupBottomNavigation() = with(binding.fragmentContainerBottomNavigation) {
        binding.root.onApplyInsets { _, insets ->
            updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
        }
        setBackgroundColor(monet.getBackgroundColor(context))
        val color = if(requireContext().isDarkMode){
            monet.getMonetColors().neutral2[800]?.toArgb()
        }else{
            monet.getMonetColors().neutral2[100]?.toArgb()
        } ?: monet.getBackgroundColor(requireContext())
        val indicatorColor = if(requireContext().isDarkMode){
            monet.getMonetColors().accent2[700]?.toArgb()
        }else{
            monet.getMonetColors().accent2[200]?.toArgb()
        }
        setBackgroundColor(ColorUtils.setAlphaComponent(color, 235))
        itemActiveIndicatorColor = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf()),
            intArrayOf(indicatorColor ?: Color.TRANSPARENT, Color.TRANSPARENT)
        )
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            //The onItemClicked flow can only be attached once due to its callbacks, so we share it to split the logic
            val flows = onItemClicked().shareIn(lifecycleScope, SharingStarted.Eagerly)
            launch {
                flows.collect {
                    viewModel.onNavigationItemClicked(it)
                }
            }
            launch {
                flows.distinctUntilChanged().collect {
                    binding.appBar.setExpanded(true)
                }
            }
        }
    }

    private fun setupCollapsingToolbar() = with(binding.collapsingToolbar) {
        setBackgroundColor(monet.getBackgroundColor(requireContext()))
        setContentScrimColor(monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(requireContext()))
        setExpandedTitleTypeface(googleSansMedium)
        setCollapsedTitleTypeface(googleSansMedium)
    }

    private fun setupToolbar() = with(binding.toolbar) {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            onNavigationIconClicked().collect {
                (navHostFragment.getTopFragment() as? ProvidesBack)?.let {
                    if(it.onBackPressed()) return@collect
                }
                viewModel.onBackPressed()
            }
        }
    }

    private fun setupBack() {
        val callback = requireActivity().onBackPressedDispatcher.addCallback(
            this,
            shouldBackDispatcherBeEnabled()
        ) {
            (navHostFragment.getTopFragment() as? ProvidesBack)?.let {
                if(it.onBackPressed()) return@addCallback
            }
            if(!navController.popBackStack()) {
                requireActivity().finish()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            navController.onDestinationChanged().collect {
                callback.isEnabled = shouldBackDispatcherBeEnabled()
            }
        }
    }

    private fun shouldBackDispatcherBeEnabled(): Boolean {
        return navHostFragment.getTopFragment() is ProvidesBack || navController.hasBackAvailable()
    }

    private fun setupNavigation() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        launch {
            navHostFragment.setupWithNavigation(navigation)
        }
        launch {
            navController.onDestinationChanged().collect {
                val label = it.label
                if(label == null || label.isBlank()) return@collect
                binding.collapsingToolbar.title = label
                binding.toolbar.title = label
            }
        }
    }

    private fun setupStack() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        navController.onDestinationChanged().collect {
            binding.root.awaitPost()
            onTopFragmentChanged(navHostFragment.getTopFragment() ?: return@collect)
        }
    }

    private fun setupCollapsedState() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.appBar.collapsedState().collect {
            navHostFragment.getTopFragment()?.rememberAppBarCollapsed(it)
        }
    }

    private fun onTopFragmentChanged(topFragment: Fragment){
        val backIcon = if(topFragment is BackAvailable){
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
        } else null
        if(topFragment is ProvidesOverflow){
            setupMenu(topFragment)
        }else{
            setupMenu(null)
        }
        if(topFragment is LockCollapsed) {
            binding.appBar.setExpanded(false)
        }else {
            binding.appBar.setExpanded(!topFragment.getRememberedAppBarCollapsed())
        }
        (topFragment as? ProvidesTitle)?.let {
            val label = it.getTitle()
            if(label == null || label.isBlank()) return@let
            binding.collapsingToolbar.title = label
            binding.toolbar.title = label
        }
        binding.toolbar.navigationIcon = backIcon
        viewModel.setCanShowSnackbar(topFragment is CanShowSnackbar)
    }

    private fun setupMenu(menuProvider: ProvidesOverflow?){
        val menu = binding.toolbar.menu
        val menuInflater = MenuInflater(requireContext())
        menu.clear()
        menuProvider?.inflateMenu(menuInflater, menu)
        binding.toolbar.setOnMenuItemClickListener {
            menuProvider?.onMenuItemSelected(it) ?: false
        }
    }

    private fun setupFabState() {
        binding.fragmentContainerBottomFab.setTint(monet.getSecondaryColor(requireContext()))
        binding.fragmentContainerBottomFabContainer.onApplyInsets { view, insets ->
            view.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
        }
        handleFabState(sharedViewModel.fabState.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            sharedViewModel.fabState.collect {
                handleFabState(it)
            }
        }
    }

    private fun handleFabState(fabState: ContainerSharedViewModel.FabState) {
        when(fabState){
            is Hidden -> {
                binding.fragmentContainerBottomFabContainer.alpha = 0f
                binding.fragmentContainerBottomFab.hide()
                binding.fragmentContainerBottomFab.setOnClickListener(null)
            }
            is Shown -> {
                binding.fragmentContainerBottomFabContainer.alpha = 1f
                binding.fragmentContainerBottomFab.show()
                binding.fragmentContainerBottomFab.text = getString(fabState.action.labelRes)
                binding.fragmentContainerBottomFab.setIconResource(fabState.action.iconRes)
                binding.fragmentContainerBottomFab.setOnClickListener {
                    sharedViewModel.onFabClicked(fabState.action)
                }
            }
        }
    }

    private fun setupSnackbar() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        sharedViewModel.snackbarBus.collect {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).apply {
                applyMonet()
                anchorView = binding.fragmentContainerBottomNavigation
                isAnchorViewLayoutListenerEnabled = true
                setTypeface(googleSansTextMedium)
                (view.background as? GradientDrawable)?.cornerRadius = resources.getDimension(R.dimen.snackbar_corner_radius)
            }.show()
        }
    }

    private fun setupUpdateSnackbar() {
        handleUpdateSnackbar(viewModel.showUpdateSnackbar.value)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.showUpdateSnackbar.collect {
                handleUpdateSnackbar(it)
            }
        }
    }

    private fun handleUpdateSnackbar(show: Boolean){
        if(show){
            updateSnackbar.show()
        }else{
            updateSnackbar.dismiss()
        }
    }

}