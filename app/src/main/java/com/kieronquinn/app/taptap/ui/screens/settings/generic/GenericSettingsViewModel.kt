package com.kieronquinn.app.taptap.ui.screens.settings.generic

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.google.android.material.slider.LabelFormatter
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import kotlinx.coroutines.flow.*

abstract class GenericSettingsViewModel : ViewModel() {

    open val restartService: Flow<Unit>? = null

    /**
     *  Combines a set of [TapTapSettings] into a single Flow, and then calls on to [restartServiceCombine]
     *  with flows
     */
    protected fun restartServiceCombine(vararg setting: TapTapSettings.TapTapSetting<*>): Flow<Unit> {
        return restartServiceCombine(*setting.map { it.asFlow().map {  } }.toTypedArray())
    }

    /**
     *  Combines a set of Flows single Flow, dropping the first emission
     *  (the initial load) and emitting the following ones with a debounce of 0.5s
     */
    protected fun restartServiceCombine(vararg flows: Flow<Unit>): Flow<Unit> {
        return combine(*flows) { }.drop(1).debounce(500L)
    }

    sealed class SettingsItem(
        val type: SettingsItemType,
        open val isVisible: () -> Boolean,
        open val isEnabled: () -> Boolean
    ) {

        data class Text(
            @DrawableRes val icon: Int,
            @StringRes val titleRes: Int? = null,
            val title: CharSequence? = null,
            @StringRes val contentRes: Int? = null,
            val content: (() -> CharSequence)? = null,
            val onClick: (() -> Unit)? = null,
            val linkClicked: ((String) -> Unit)? = null,
            override val isVisible: () -> Boolean = { true },
            override val isEnabled: () -> Boolean = { true }
        ) : SettingsItem(SettingsItemType.TEXT, isVisible, isEnabled)

        data class Switch(
            @DrawableRes val icon: Int,
            @StringRes val titleRes: Int? = null,
            val title: CharSequence? = null,
            @StringRes val contentRes: Int? = null,
            val content: (() -> CharSequence)? = null,
            val setting: TapTapSettings.TapTapSetting<Boolean>,
            override val isVisible: () -> Boolean = { true },
            override val isEnabled: () -> Boolean = { true }
        ) : SettingsItem(SettingsItemType.SWITCH, isVisible, isEnabled)

        data class Slider(
            @DrawableRes val icon: Int,
            @StringRes val titleRes: Int? = null,
            val title: CharSequence? = null,
            @StringRes val contentRes: Int? = null,
            val content: (() -> CharSequence)? = null,
            val setting: TapTapSettings.TapTapSetting<Number>,
            val minimumValue: Float = 0f,
            val maximumValue: Float = 10f,
            val stepSize: Float? = null,
            val labelFormatter: LabelFormatter? = null,
            override val isVisible: () -> Boolean = { true },
            override val isEnabled: () -> Boolean = { true }
        ) : SettingsItem(SettingsItemType.SLIDER, isVisible, isEnabled)

        data class Info(
            @StringRes val contentRes: Int? = null,
            val content: (() -> CharSequence)? = null,
            val onClick: (() -> Unit)? = null,
            override val isVisible: () -> Boolean = { true },
            override val isEnabled: () -> Boolean = { true },
            @DrawableRes val icon: Int? = null,
            val linkClicked: ((String) -> Unit)? = null,
            val onDismissClicked: (() -> Unit)? = null,
        ) : SettingsItem(SettingsItemType.INFO, isVisible, isEnabled)

        data class About(
            val onContributorsClicked: () -> Unit,
            val onDonateClicked: () -> Unit,
            val onGitHubClicked: () -> Unit,
            val onTwitterClicked: () -> Unit,
            val onXdaClicked: () -> Unit,
            val onLibrariesClicked: () -> Unit
        ): SettingsItem(SettingsItemType.ABOUT, { true }, { true })

        data class Header(
            @StringRes val titleRes: Int
        ): SettingsItem(SettingsItemType.HEADER, { true }, { true })

        enum class SettingsItemType {
            TEXT, SWITCH, SLIDER, INFO, ABOUT, HEADER;

            companion object {
                fun fromViewType(type: Int): SettingsItemType {
                    return values()[type]
                }
            }

        }

    }

}