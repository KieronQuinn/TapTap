package com.kieronquinn.app.taptap.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceScreen
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.preferences.Preference

class Links {
    companion object {
        const val LINK_GITHUB = "https://kieronquinn.co.uk/redirect/TapTap/github"
        const val LINK_XDA = "https://kieronquinn.co.uk/redirect/TapTap/xda"
        const val LINK_DONATE = "https://kieronquinn.co.uk/redirect/TapTap/donate"
        const val LINK_TWITTER = "https://kieronquinn.co.uk/redirect/TapTap/twitter"

        private fun startLinkIntent(context: Context, link: String){
            if(link == LINK_XDA){
                startLinkIntentXDA(context)
            }else{
                startCCT(context, link)
            }
        }

        private fun startLinkIntentXDA(context: Context){
            if(context.isAppLaunchable("com.xda.labs")){
                //Open normally rather than CCT to allow for XDA app to open forum link
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(LINK_XDA)
                context.startActivity(intent)
            }else {
                startCCT(context, LINK_XDA)
            }
        }

        private fun startCCT(context: Context, link: String){
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(context, R.color.windowBackground))
                .build()
            customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            customTabsIntent.launchUrl(context, Uri.parse(link))
        }

        fun setupPreference(context: Context, preferenceScreen: PreferenceScreen, preferenceKey: String, link: String){
            val preference = preferenceScreen.findPreference<Preference>(preferenceKey)
            preference?.onPreferenceClickListener = androidx.preference.Preference.OnPreferenceClickListener {
                startLinkIntent(context, link)
                true
            }
        }
    }
}
