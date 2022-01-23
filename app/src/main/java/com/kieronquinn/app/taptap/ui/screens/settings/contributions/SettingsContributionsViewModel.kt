package com.kieronquinn.app.taptap.ui.screens.settings.contributions

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.taptap.components.navigation.ContainerNavigation
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel
import kotlinx.coroutines.launch

abstract class SettingsContributionsViewModel: GenericSettingsViewModel() {

    abstract fun onLinkClicked(url: String)

}

class SettingsContributionsViewModelImpl(private val navigation: ContainerNavigation): SettingsContributionsViewModel() {

    override fun onLinkClicked(url: String) {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            })
        }
    }

}