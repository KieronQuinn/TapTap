package com.kieronquinn.app.taptap.core.columbus.actions

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.kieronquinn.app.taptap.models.WhenGateInternal
import com.kieronquinn.app.taptap.core.services.TapAccessibilityService
import com.kieronquinn.app.taptap.utils.extensions.isPackageAssistant

/**
 * Starts a Google Search similar to the search bar in Launcher3.
 *
 * See https://stackoverflow.com/a/36922412/4421500
 */
class LaunchSearch(context: Context, whenGates: List<WhenGateInternal>) : ActionBase(
    context,
    whenGates
) {

    override val requiresUnlock: Boolean = true

    override fun isAvailable(): Boolean {
        val accessibilityService = context as TapAccessibilityService
        return !context.isPackageAssistant(accessibilityService.getCurrentPackageName()) && super.isAvailable()
    }

    override fun onTrigger() {
        super.onTrigger()

        val searchManager = context.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val globalSearchActivity = searchManager.globalSearchActivity ?: return

        val intent = Intent(SearchManager.INTENT_ACTION_GLOBAL_SEARCH).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            component = globalSearchActivity

            putExtra(SearchManager.QUERY, "")
            putExtra(SearchManager.EXTRA_SELECT_QUERY, true)

            val appSearchData = Bundle()
            appSearchData.putString("source", context.packageName)

            putExtra(SearchManager.APP_DATA, appSearchData)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

}
