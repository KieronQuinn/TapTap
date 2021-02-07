package com.kieronquinn.app.taptap.ui.screens.settings.about

import android.content.Context
import android.content.Intent
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavOptions
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.extensions.getResourceIdArray
import com.kieronquinn.app.taptap.utils.launchCCT
import com.kieronquinn.app.taptap.utils.extensions.toActivityDestination
import com.kieronquinn.app.taptap.utils.extensions.withStandardAnimations
import com.kieronquinn.app.taptap.components.base.BaseViewModel

class SettingsAboutViewModel: BaseViewModel() {

    companion object {
        private const val LINK_GITHUB = "https://kieronquinn.co.uk/redirect/TapTap/github"
        private const val LINK_XDA = "https://kieronquinn.co.uk/redirect/TapTap/xda"
    }

    fun getItems(context: Context): Array<SettingsAboutAdapter.AboutItem> {
        val items = mutableListOf(
            SettingsAboutAdapter.AboutItem.Header,
            SettingsAboutAdapter.AboutItem.Title(R.string.about_contributors),
            SettingsAboutAdapter.AboutItem.Item(R.string.about_contributors_main, R.string.about_contributors_main_content, R.drawable.ic_about_taptap_logo, isHtml = true),
            SettingsAboutAdapter.AboutItem.Item(R.string.about_contributors_community, R.string.about_contributors_community_content, R.drawable.ic_about_github),
            SettingsAboutAdapter.AboutItem.Item(R.string.about_contributors_icons, R.string.about_contributors_icons_content, R.drawable.ic_about_icons, isHtml = true),
            SettingsAboutAdapter.AboutItem.Item(R.string.about_contributors_font, R.string.about_contributors_font_content, R.drawable.ic_about_font, isHtml = true),
            SettingsAboutAdapter.AboutItem.Title(R.string.about_translators)
        )
        val flags = context.resources.getResourceIdArray(R.array.about_translators_flags)
        val contents = context.resources.getResourceIdArray(R.array.about_translators_content)
        for((index, country) in context.resources.getResourceIdArray(R.array.about_translators_headings).withIndex()){
            val flag = flags[index]
            val content = contents[index]
            items.add(SettingsAboutAdapter.AboutItem.Item(country, content, flag))
        }
        return items.toTypedArray()
    }

    fun onXDAClicked(context: Context) = context.run {
        launchCCT(LINK_XDA)
    }

    fun onGitHubClicked(context: Context) = context.run {
        launchCCT(LINK_GITHUB)
    }

    fun onLinkClicked(context: Context, url: String) = context.run {
        launchCCT(url)
    }

    fun onLibrariesClicked(context: Context) = context.run {
        OssLicensesMenuActivity.setActivityTitle(getString(R.string.libraries))
        Intent(context, OssLicensesMenuActivity::class.java).toActivityDestination(this).run {
            ActivityNavigator(context).navigate(this, null, NavOptions.Builder().withStandardAnimations().build(), null)
        }
    }

}