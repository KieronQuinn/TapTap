package com.kieronquinn.app.taptap.repositories.phonespecs

import android.content.Context
import android.os.Build
import com.google.gson.Gson
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettings
import com.kieronquinn.app.taptap.models.phonespecs.DeviceSpecs
import com.kieronquinn.app.taptap.service.retrofit.createPhoneSpecsService
import com.kieronquinn.app.taptap.utils.extensions.closestValueBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface PhoneSpecsRepository {

    /**
     *  Get device specs (name, image, height in `mm` and height in `in`) if available, using the
     *  device model from the build.prop
     *
     *  This uses an external API, which may be unreliable, blocked or taken down (as it appears to
     *  scrape GSMArena), so this method returning `null` is not a guarantee that the model is not
     *  real.
     */
    suspend fun getDeviceSpecs(model: String = Build.MODEL): DeviceSpecs?

    /**
     *  Get the best models for a given device height as a pair (new & legacy)
     */
    fun getBestModels(deviceSpecs: DeviceSpecs): Pair<TapModel, TapModel>

}

class PhoneSpecsRepositoryImpl(context: Context, private val gson: Gson, private val settings: TapTapSettings) : PhoneSpecsRepository {

    private val cacheDir = File(context.cacheDir, "specs")

    private val phoneSpecsService = createPhoneSpecsService()

    private val deviceDimensionsRegex by lazy {
        "(.*) mm, (.*) inch".toRegex()
    }

    private fun getCachedSpecs(model: String): DeviceSpecs? {
        val cachedJson = File(cacheDir, "$model.json")
        if(!cachedJson.exists()) return null
        return try {
            gson.fromJson(cachedJson.readText(), DeviceSpecs::class.java)
        }catch (e: Exception){
            //Re-get the specs
            null
        }
    }

    private fun saveSpecsToCache(model: String, deviceSpecs: DeviceSpecs) {
        cacheDir.mkdirs()
        val cacheJson = File(cacheDir, "$model.json")
        cacheJson.writeText(gson.toJson(deviceSpecs))
    }

    override suspend fun getDeviceSpecs(model: String): DeviceSpecs? = withContext(Dispatchers.IO) {
        try {
            getCachedSpecs(model)?.let {
                return@withContext it
            }
            //Don't continue if internet access is not allowed
            if(!settings.internetAllowed.get()) return@withContext null
            val specs = phoneSpecsService.getModelSpecs(model).execute().body()
                ?: return@withContext null
            val name = specs.name ?: return@withContext null
            val height = specs.height ?: return@withContext null
            val image = specs.image
            //Parse out the dimensions
            val dimensions = deviceDimensionsRegex.matchEntire(height)?.groupValues
                ?: return@withContext null
            val heightMm = dimensions[1].toDoubleOrNull() ?: return@withContext null
            val heightIn = dimensions[2].toDoubleOrNull() ?: return@withContext null
            return@withContext DeviceSpecs(name, image, heightMm, heightIn).also {
                saveSpecsToCache(model, it)
            }
        } catch (e: Exception) {
            return@withContext null
        }
    }

    override fun getBestModels(deviceSpecs: DeviceSpecs): Pair<TapModel, TapModel> {
        val newModel = TapModel.values().filter { it.modelType == TapModel.ModelType.NEW }
            .closestValueBy(deviceSpecs.heightMm) { it.deviceHeight }
        val legacyModel = TapModel.values().filter { it.modelType == TapModel.ModelType.LEGACY }
            .closestValueBy(deviceSpecs.heightMm) { it.deviceHeight }
        return Pair(newModel, legacyModel)
    }

}