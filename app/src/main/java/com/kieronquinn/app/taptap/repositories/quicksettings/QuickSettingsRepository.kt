package com.kieronquinn.app.taptap.repositories.quicksettings

import android.content.ComponentName
import android.content.Context
import android.os.Parcelable
import android.provider.Settings
import com.kieronquinn.app.taptap.repositories.quicksettings.QuickSettingsRepository.QuickSetting
import com.kieronquinn.app.taptap.utils.extensions.Settings_Secure_getStringSafely
import com.kieronquinn.app.taptap.utils.extensions.getApplicationLabel
import com.kieronquinn.app.taptap.utils.extensions.getServiceLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

interface QuickSettingsRepository {

    @Parcelize
    data class QuickSetting(val component: ComponentName, val label: CharSequence?, val packageLabel: CharSequence): Parcelable
    suspend fun getQuickSettings(): List<QuickSetting>

}

class QuickSettingsRepositoryImpl(context: Context) : QuickSettingsRepository {

    companion object {
        private const val SETTINGS_SECURE_QUICK_SETTINGS = "sysui_qs_tiles"
        private val QS_TILE_CUSTOM_REGEX = "custom\\((.*)/(.*)\\)".toRegex()
    }

    private val contentResolver = context.contentResolver
    private val packageManager = context.packageManager

    /**
     *  Loads a list of **custom** Quick Settings Tiles that are added on the device.
     *  In theory, `cmd statusbar click-tile` can click *any* tile, not just those that are added,
     *  but due to limitations the list returned to the app would be pretty much unusable as the
     *  labels are not meant to be user-facing. Instead, we show just the ones added by the user,
     *  with a note asking them to check the notification shade to make sure they select the right
     *  one.
     */
    override suspend fun getQuickSettings(): List<QuickSetting> = withContext(Dispatchers.IO) {
        val qsTiles = Settings_Secure_getStringSafely(contentResolver, SETTINGS_SECURE_QUICK_SETTINGS)
            ?: return@withContext emptyList()
        if (qsTiles.isBlank()) return@withContext emptyList()
        val components = qsTiles.commaSeparated().mapNotNull {
            val matcher = QS_TILE_CUSTOM_REGEX.find(it) ?: return@mapNotNull null
            ComponentName(matcher.groupValues[1], matcher.groupValues[2])
        }
        return@withContext components.mapNotNull {
            //For some reason flattening and then unflattening makes getServiceLabel work better
            val formattedComponent = ComponentName.unflattenFromString(it.flattenToString())
            QuickSetting(it, packageManager.getServiceLabel(formattedComponent), packageManager.getApplicationLabel(it.packageName) ?: return@mapNotNull null)
        }
    }

    private fun String.commaSeparated(): List<String> {
        if (!contains(",")) return listOf(this)
        return split(",")
    }

}