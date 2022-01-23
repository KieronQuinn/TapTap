package com.kieronquinn.app.taptap.repositories.snapchat

import android.content.Context
import android.provider.Settings
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.kieronquinn.app.taptap.models.service.SnapchatQuickTapState
import com.kieronquinn.app.taptap.models.settings.ColumbusPackageStats
import com.kieronquinn.app.taptap.models.settings.hasGrantedColumbusForPackage
import com.kieronquinn.app.taptap.repositories.service.TapTapRootServiceRepository
import com.kieronquinn.app.taptap.utils.extensions.Settings_Secure_getStringSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 *  Local & root proxy checks for the availability Quick Tap to Snap, so relevant steps can be
 *  shown to set it up if needed.
 */
abstract class SnapchatRepository {

    abstract suspend fun getQuickTapToSnapState(): QuickTapToSnapState
    abstract suspend fun applyOverride()

    enum class QuickTapToSnapState {
        UNAVAILABLE, AVAILABLE, NEEDS_SETUP, NEEDS_SETUP_ROOT, ERROR
    }

}

class SnapchatRepositoryImpl(
    context: Context,
    private val gson: Gson,
    private val rootServiceRepository: TapTapRootServiceRepository
) : SnapchatRepository() {

    companion object {
        private const val FEATURE_QUICK_TAP = "com.google.android.feature.QUICK_TAP"
        private const val PACKAGE_SNAPCHAT = "com.snapchat.android"
    }

    private val contentResolver = context.contentResolver
    private val packageManager = context.packageManager

    override suspend fun getQuickTapToSnapState(): QuickTapToSnapState {
        return when {
            //Pixel & Approved already
            doesHaveQuickTapFeature() && doesPackageStatsContainApproval() -> QuickTapToSnapState.AVAILABLE
            //Pixel but not approved (show instructions)
            doesHaveQuickTapFeature() -> QuickTapToSnapState.NEEDS_SETUP
            else -> getRootQuickTapToSnapState()
        }
    }

    override suspend fun applyOverride() {
        withContext(Dispatchers.IO){
            rootServiceRepository.runWithService {
                it.applySnapchatOverride()
            }
        }.also {
            rootServiceRepository.unbindServiceIfNeeded()
        }
    }

    private suspend fun getRootQuickTapToSnapState(): QuickTapToSnapState =
        withContext(Dispatchers.IO) {
            val rootState = getRootQuickTapToSnapStateRoot()
            rootState?.let { state ->
                return@withContext when (state) {
                    //Root method already followed
                    SnapchatQuickTapState.ENABLED -> QuickTapToSnapState.AVAILABLE
                    //Root is available, but method needs following
                    SnapchatQuickTapState.DISABLED -> QuickTapToSnapState.NEEDS_SETUP_ROOT
                    //Failed to query the provider
                    SnapchatQuickTapState.ERROR -> QuickTapToSnapState.ERROR
                    //Root not available
                    SnapchatQuickTapState.NO_ROOT -> QuickTapToSnapState.UNAVAILABLE
                }
            }
            //Service not available (presume no root)
            return@withContext QuickTapToSnapState.UNAVAILABLE
        }.also {
            rootServiceRepository.unbindServiceIfNeeded()
        }

    private suspend fun getRootQuickTapToSnapStateRoot(): SnapchatQuickTapState? {
        return rootServiceRepository.runWithService {
            return@runWithService it.isSnapchatQuickTapToSnapEnabled
        }
    }

    private fun doesHaveQuickTapFeature(): Boolean {
        return packageManager.hasSystemFeature(FEATURE_QUICK_TAP)
    }

    private fun doesPackageStatsContainApproval(): Boolean {
        val columbusPackageStats =
            Settings_Secure_getStringSafely(contentResolver, "columbus_package_stats")
        if (columbusPackageStats.isNullOrBlank()) return false
        val packageStats = try {
            gson.fromJson(columbusPackageStats, Array<ColumbusPackageStats>::class.java)
        } catch (e: JsonParseException) {
            return false
        }
        return packageStats.hasGrantedColumbusForPackage(PACKAGE_SNAPCHAT)
    }

}