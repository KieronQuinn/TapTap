package com.kieronquinn.app.taptap.ui.screens.settings.nativemode

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.databinding.FragmentSettingsNativeModeBinding
import com.kieronquinn.app.taptap.ui.activities.MainActivity
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.base.BoundFragment
import com.kieronquinn.app.taptap.ui.screens.container.ContainerSharedViewModel
import com.kieronquinn.app.taptap.utils.extensions.applyBottomInsets
import com.kieronquinn.app.taptap.utils.extensions.onClicked
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationChannel
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationId
import com.kieronquinn.app.taptap.utils.notifications.TapTapNotificationIntentId
import com.kieronquinn.monetcompat.view.MonetSwitch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsNativeModeFragment: BoundFragment<FragmentSettingsNativeModeBinding>(FragmentSettingsNativeModeBinding::inflate), BackAvailable {

    private val viewModel by viewModel<SettingsNativeModeViewModel>()
    private val containerViewModel by sharedViewModel<ContainerSharedViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        containerViewModel.setSuppressColumbusRestart(true)
        setupSwitch()
        setupSwitchEnabled()
        setupSwitchChecked()
        setupAutomaticSetupButton()
        setupManualSetupButton()
        setupSetup()
        setupScrollView()
        setupToastBus()
        setupNotification()
    }

    override fun onDestroyView() {
        containerViewModel.setSuppressColumbusRestart(false)
        super.onDestroyView()
    }

    private fun setupSwitch() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsNativeModeEnable.onClicked().collect {
            viewModel.onSwitchClicked(it as MonetSwitch)
        }
    }

    private fun setupSwitchChecked() {
        binding.settingsNativeModeEnable.isChecked = viewModel.nativeModeEnabled.value
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.nativeModeEnabled.collect {
                binding.settingsNativeModeEnable.isChecked = it
            }
        }
    }

    private fun setupSwitchEnabled() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.switchEnabled.collect {
            binding.settingsNativeModeEnable.isEnabled = it
            binding.settingsNativeModeEnable.alpha = if(it) 1f else 0.5f
        }
    }

    private fun setupSetup() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.isSetupRequired.collect {
            binding.settingsNativeModeErrorSetup.isVisible = it
        }
    }

    private fun setupManualSetupButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsNativeModeErrorButtonManual.onClicked().collect {
            viewModel.onManualSetupClicked()
        }
    }

    private fun setupAutomaticSetupButton() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        binding.settingsNativeModeErrorButtonAutomatic.onClicked().collect {
            viewModel.onAutomaticSetupClicked(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkState(requireContext())
    }

    private fun setupScrollView() = with(binding.settingsNativeModeScrollView) {
        applyBottomInsets(binding.root)
    }

    private fun setupToastBus() = viewLifecycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.toastBus.collect {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupNotification() = lifecycleScope.launchWhenCreated {
        viewModel.setupNotificationBus.collect {
            showSetupNotification()
        }
    }

    private suspend fun showSetupNotification() = with(TapTapNotificationChannel.NativeSetup) {
        showNotification(
            requireContext(),
            TapTapNotificationId.NATIVE_SETUP
        ){
            val clickIntent = Intent(requireContext(), MainActivity::class.java)
            val content = getString(R.string.notification_native_setup_content)
            it.setOngoing(false)
            it.setSmallIcon(TapTapNotificationChannel.NOTIFICATION_ICON)
            it.setContentTitle(getString(R.string.notification_native_setup_title))
            it.setContentText(content)
            it.setAutoCancel(true)
            it.setCategory(Notification.CATEGORY_REMINDER)
            it.setStyle(NotificationCompat.BigTextStyle(it).bigText(content))
            it.setContentIntent(PendingIntent.getActivity(context, TapTapNotificationIntentId.NATIVE_SETUP_CLICK.ordinal, clickIntent, PendingIntent.FLAG_IMMUTABLE))
            it.priority = NotificationCompat.PRIORITY_MAX
        }
        //Clear notification after 5 seconds
        delay(5000L)
        cancelNotifications(requireContext(), TapTapNotificationId.NATIVE_SETUP)
    }

}