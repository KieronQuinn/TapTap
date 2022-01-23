package com.kieronquinn.app.taptap.ui.screens.settings.contributions

import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.ui.base.BackAvailable
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsAdapter
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsFragment
import com.kieronquinn.app.taptap.ui.screens.settings.generic.GenericSettingsViewModel.SettingsItem
import com.kieronquinn.app.taptap.utils.extensions.getResourceIdArray
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsContributionsFragment: GenericSettingsFragment(), BackAvailable {

    override val viewModel by viewModel<SettingsContributionsViewModel>()

    override val items by lazy {
        listOf(
            SettingsItem.Text(
                icon = R.drawable.ic_gesture,
                titleRes = R.string.about_contributors_main,
                contentRes = R.string.about_contributors_main_content,
                linkClicked = viewModel::onLinkClicked
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_community,
                titleRes = R.string.about_contributors_community,
                contentRes = R.string.about_contributors_community_content
            ),
            SettingsItem.Text(
                icon = R.drawable.ic_contributions_icons,
                titleRes = R.string.about_contributors_icons,
                contentRes = R.string.about_contributors_icons_content,
                linkClicked = viewModel::onLinkClicked
            ),
            SettingsItem.Header(R.string.about_translators),
            *getTranslatorsList().toTypedArray()
        )
    }

    override fun createAdapter(items: List<SettingsItem>): GenericSettingsAdapter {
        return SettingsContributionsAdapter()
    }

    private fun getTranslatorsList(): List<SettingsItem.Text> {
        val headings = resources.getResourceIdArray(R.array.about_translators_headings)
        val content = resources.getResourceIdArray(R.array.about_translators_content)
        val flags = resources.getResourceIdArray(R.array.about_translators_flags)
        return headings.mapIndexed { index, resource ->
            SettingsItem.Text(
                icon = flags[index],
                titleRes = resource,
                contentRes = content[index]
            )
        }
    }

    inner class SettingsContributionsAdapter: GenericSettingsAdapter(requireContext(), binding.settingsGenericRecyclerView, items)

}