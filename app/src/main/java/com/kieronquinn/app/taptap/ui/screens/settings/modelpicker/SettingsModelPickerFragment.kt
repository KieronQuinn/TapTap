package com.kieronquinn.app.taptap.ui.screens.settings.modelpicker

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsModelPickerBinding
import com.kieronquinn.app.taptap.databinding.ItemSettingsModelPickerDeviceBinding
import com.kieronquinn.app.taptap.models.phonespecs.DeviceSpecs
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.base.LockCollapsed
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.ui.screens.settings.modelpicker.SettingsModelPickerViewModel.State
import com.kieronquinn.app.taptap.utils.extensions.applyBackgroundTint
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.isDarkMode
import com.kieronquinn.app.taptap.utils.extensions.onSelected
import com.kieronquinn.app.taptap.utils.extensions.selectTab
import com.kieronquinn.app.taptap.utils.extensions.whenResumed
import com.kieronquinn.monetcompat.extensions.toArgb
import com.kieronquinn.monetcompat.extensions.views.applyMonet
import com.squareup.picasso.Picasso
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsModelPickerFragment :
    BoundFragment<FragmentSettingsModelPickerBinding>(FragmentSettingsModelPickerBinding::inflate), LockCollapsed, BackAvailable {

    private val viewModel by viewModel<SettingsModelPickerViewModel>()
    private val sharedViewModel by sharedViewModel<ContainerSharedViewModel>()

    private val adapter by lazy {
        SettingsModelPickerAdapter(binding.settingsModelPickerRecyclerview, emptyList(), viewModel::onModelSelected)
    }

    private val picasso by lazy {
        Picasso.get()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        setupRecyclerView()
        setupMonet()
        setupTabs()
        setupReload()
    }

    private fun setupState(){
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun setupRecyclerView() = with(binding.settingsModelPickerRecyclerview) {
        layoutManager = LinearLayoutManager(context)
        adapter = this@SettingsModelPickerFragment.adapter
        applyBottomInsets(binding.root)
    }

    private fun setupMonet() {
        val tabBackground = monet.getMonetColors().accent1[600]?.toArgb() ?: monet.getAccentColor(requireContext(), false)
        binding.settingsModelPickerTabs.backgroundTintList = ColorStateList.valueOf(tabBackground)
        binding.settingsModelPickerTabs.setSelectedTabIndicatorColor(monet.getAccentColor(requireContext()))
        binding.settingsModelPickerLoadingProgress.applyMonet()
        val secondaryBackground = ColorStateList.valueOf(monet.getBackgroundColorSecondary(requireContext()) ?: monet.getBackgroundColor(requireContext()))
        binding.settingsModelPickerAppbar.backgroundTintList = secondaryBackground
        binding.settingsModelPickerTabsContainer.backgroundTintList = secondaryBackground
        if (requireContext().isDarkMode) R.color.cardview_dark_background else R.color.cardview_light_background
        binding.settingsModelPickerDevice.root.applyBackgroundTint(monet)
    }

    private fun setupTabs() = whenResumed {
        binding.settingsModelPickerTabs.onSelected().collect {
            viewModel.onTabSelected(it)
        }
    }

    private fun handleState(state: State){
        when(state){
            is State.Loading -> {
                binding.settingsModelPickerLoading.isVisible = true
                binding.settingsModelPickerLoaded.isVisible = false
                binding.settingsModelPickerAppbar.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsModelPickerLoading.isVisible = false
                binding.settingsModelPickerLoaded.isVisible = true
                binding.settingsModelPickerAppbar.isVisible = state.deviceSpecs != null
                binding.settingsModelPickerRecyclerview.isNestedScrollingEnabled = state.deviceSpecs != null
                binding.settingsModelPickerTabsContainer.alpha = 1f
                binding.settingsModelPickerDevice.setupWithSpecs(state.deviceSpecs)
                binding.settingsModelPickerTabs.selectTab(state.selectedTab ?: 0)
                adapter.items = state.items
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun ItemSettingsModelPickerDeviceBinding.setupWithSpecs(specs: DeviceSpecs?) {
        if(specs == null) return
        itemSettingsActionsActionTitle.text = specs.deviceName
        itemSettingsActionsActionContent.text = getString(R.string.settings_model_picker_device_size, specs.heightMm.toString(), specs.heightIn.toString())
        if(specs.deviceImageUrl != null) {
            itemSettingsActionsActionIcon.isVisible = true
            picasso.load(specs.deviceImageUrl).into(itemSettingsActionsActionIcon)
        }else {
            itemSettingsActionsActionTitle.updatePadding(left = 0)
            itemSettingsActionsActionContent.updatePadding(left = 0)
            itemSettingsActionsActionIcon.isVisible = false
        }
    }

    private fun setupReload() = whenResumed {
        viewModel.restartService?.collect {
            sharedViewModel.restartService(requireContext())
        }
    }

}