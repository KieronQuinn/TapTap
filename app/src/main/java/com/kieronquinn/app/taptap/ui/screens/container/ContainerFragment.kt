package com.kieronquinn.app.taptap.ui.screens.container

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentContainerBinding
import com.kieronquinn.app.taptap.utils.*
import com.kieronquinn.app.taptap.utils.extensions.animateColorChange
import com.kieronquinn.app.taptap.utils.extensions.animateElevationChange
import com.kieronquinn.app.taptap.utils.extensions.dip
import com.kieronquinn.app.taptap.utils.extensions.observe
import com.kieronquinn.app.taptap.components.base.BaseFragment
import com.kieronquinn.app.taptap.components.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.update.UpdateBottomSheetFragment
import dev.chrisbanes.insetter.applySystemWindowInsetsToPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ContainerFragment: BoundFragment<FragmentContainerBinding>(FragmentContainerBinding::class.java) {

    private val viewModel by sharedViewModel<ContainerViewModel>()
    private val updateChecker by inject<UpdateChecker>()

    private val navHostFragment by lazy {
        childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }

    private val splashGraph by lazy {
        navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph_splash)
    }

    private val setupGraph by lazy {
        navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph_setup)
    }

    private val settingsGraph by lazy {
        navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnApplyWindowInsetsListener { v, insets ->
            viewModel.notifyInsetsChanged(insets)
            insets
        }
        viewModel.containerState.observe(viewLifecycleOwner){
            navHostFragment.navController.graph = when(it){
                is ContainerViewModel.ContainerState.Splash -> {
                    splashGraph
                }
                is ContainerViewModel.ContainerState.Setup -> {
                    setupGraph
                }
                is ContainerViewModel.ContainerState.Settings -> {
                    settingsGraph
                }
            }
        }
        viewModel.shouldShowToolbar.observe(viewLifecycleOwner){
            binding.toolbar.isVisible = it
        }
        viewModel.switchState.observe(viewLifecycleOwner){
            binding.switchMain.run {
                isVisible = it != ContainerViewModel.SwitchState.HIDDEN
                viewModel.getSwitchText()?.let {
                    setText(it)
                }
            }
        }
        viewModel.shouldShowMenu.observe(viewLifecycleOwner){
            binding.toolbar.run {
                menu.clear()
                if(it) inflateTapMenu()
            }
        }
        viewModel.shouldShowBack.observe(viewLifecycleOwner){
            if(it){
                binding.toolbar.setNavigationIcon(R.drawable.ic_back)
            }else{
                binding.toolbar.navigationIcon = null
            }
        }
        viewModel.switchCheckedState.observe(viewLifecycleOwner){
            binding.switchMain.isChecked = it
        }
        viewModel.shouldShowToolbarShadow.observe(viewLifecycleOwner){
            context?.setToolbarElevationEnabled(it)
        }
        viewModel.shouldDisableToolbarBackground.observe(viewLifecycleOwner){
            context?.setToolbarElevationEnabled(it)
        }
        binding.switchMain.setOnClickListener {
            viewModel.onSwitchClicked()
        }
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            viewModel.setCurrentDestination(destination.id)
            if(destination.label?.isBlank() == true) return@addOnDestinationChangedListener
            viewModel.toolbarTitle.postValue(destination.label)
        }
        viewModel.toolbarTitle.observe(viewLifecycleOwner){
            binding.toolbarTitle.text = it
        }
        with(binding.toolbar){
            applySystemWindowInsetsToPadding(top = true)
            setNavigationOnClickListener {
                onBack()
            }
            navHostFragment.childFragmentManager.addOnBackStackChangedListener {
                viewModel.notifyContainerFragmentDepthChanged(navHostFragment.childFragmentManager.backStackEntryCount)
            }
            setOnMenuItemClickListener {
                viewModel.onMenuItemSelected(it, this@ContainerFragment)
                true
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback {
            onBack()
        }
        checkForUpdates()
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                updateChecker.clearCachedDownloads(requireContext())
            }
            updateChecker.updateAvailable.collect { updateAvailable ->
                binding.toolbar.menu.findItem(R.id.menu_update)?.let {
                    it.isVisible = updateAvailable
                }
            }
        }
    }

    private fun checkForUpdates() = lifecycleScope.launch {
        updateChecker.getLatestRelease().collect {
            if(it != null && childFragmentManager.findFragmentByTag("bs_update") == null && !updateChecker.hasDismissedDialog){
                val extra = bundleOf(UpdateBottomSheetFragment.KEY_UPDATE to it)
                UpdateBottomSheetFragment().apply {
                    arguments = extra
                }.show(childFragmentManager, "bs_update")
            }
        }
    }

    private fun onBack(){
        (navHostFragment.childFragmentManager.primaryNavigationFragment as? BaseFragment)?.onBackPressed() ?: run {
            if(!navHostFragment.navController.navigateUp()) requireActivity().finish()
        }
    }

    private fun MaterialToolbar.inflateTapMenu(){
        inflateMenu(R.menu.menu_main)
        menu.findItem(R.id.menu_update)?.isVisible = updateChecker.updateAvailable.value
    }

    private var toolbarColorAnimation: ValueAnimator? = null
    private var toolbarElevationAnimation: ValueAnimator? = null

    private fun Context.setToolbarElevationEnabled(enabled: Boolean){
        val toolbarColor = when {
            viewModel.shouldDisableToolbarBackground.value == true -> {
                ContextCompat.getColor(this, android.R.color.transparent)
            }
            enabled -> {
                ContextCompat.getColor(this, R.color.toolbarColor)
            }
            else -> {
                ContextCompat.getColor(this, R.color.windowBackground)
            }
        }
        val elevation = if(enabled) dip(8).toFloat() else 0f
        val initialBeforeColor = if(toolbarColorAnimation == null){
            ContextCompat.getColor(this, R.color.toolbarColor)
        }else{
            null
        }
        toolbarColorAnimation?.cancel()
        toolbarElevationAnimation?.cancel()
        toolbarColorAnimation = binding.toolbar.animateColorChange(beforeColor = initialBeforeColor, afterColor = toolbarColor)
        binding.switchMain.animateColorChange(beforeColor = initialBeforeColor, afterColor = toolbarColor)
        toolbarElevationAnimation = binding.toolbar.animateElevationChange(elevation)
        binding.switchMain.animateElevationChange(elevation)
    }

}