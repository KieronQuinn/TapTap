package com.kieronquinn.app.taptap.repositories.phonespecs

import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.kieronquinn.app.taptap.components.settings.TapModel
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl
import com.kieronquinn.app.taptap.models.phonespecs.DeviceSpecs
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class PhoneSpecsRepositoryTest {

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    private val phoneSpecsRepository by lazy {
        PhoneSpecsRepositoryImpl(context, Gson(), TapTapSettingsImpl(context))
    }

    @Test
    fun testWithPixel6Pro() {
        runBlocking {
            val specs = phoneSpecsRepository.getDeviceSpecs("Pixel 6 Pro")
            assertNotNull("Returned specs are null (parsing failed)", specs)
            assertEquals("Device name does match Pixel 6 Pro", specs?.deviceName, "Google Pixel 6 Pro 5G")
            assertEquals("Height in mm does not match 163.9", specs?.heightMm, 163.9)
            assertEquals("Height in in does not match 6.45", specs?.heightIn, 6.45)
        }
    }

    @Test
    fun testWithPixel3XL() {
        runBlocking {
            val specs = phoneSpecsRepository.getDeviceSpecs("Pixel 3 XL")
            assertNotNull("Returned specs are null (parsing failed)", specs)
            assertEquals("Device name does match Pixel 3 XL", specs?.deviceName, "Google Pixel XL 3 Phone")
            assertEquals("Height in mm does not match 158", specs?.heightMm, 158.0)
            assertEquals("Height in in does not match 6.22", specs?.heightIn, 6.22)
        }
    }

    //Edge case: OP shipped the 7TP with no space for a while
    @Test
    fun testWithOnePlus7TPro() {
        runBlocking {
            val specs = phoneSpecsRepository.getDeviceSpecs("OnePlus 7TPro")
            assertNotNull("Returned specs are null (parsing failed)", specs)
            assertEquals("Device name does match 7T Pro", specs?.deviceName, "OnePlus 7T Pro 5G McLaren Edition")
            assertEquals("Height in mm does not match 162.6", specs?.heightMm, 162.6)
            assertEquals("Height in in does not match 6.40", specs?.heightIn, 6.40)
        }
    }

    //Edge case: Samsung's Build.MODEL is the literal model name not the user-visible name as it should be
    @Test
    fun testWithGalaxyS20Ultra5G() {
        runBlocking {
            val specs = phoneSpecsRepository.getDeviceSpecs("SM-G988N")
            assertNotNull("Returned specs are null (parsing failed)", specs)
            assertEquals("Device name does match S20 Ultra 5G", specs?.deviceName, "Samsung Galaxy S20 Ultra 5G")
            assertEquals("Height in mm does not match 166.9", specs?.heightMm, 166.9)
            assertEquals("Height in in does not match 6.57", specs?.heightIn, 6.57)
        }
    }

    //Test for a phone that's above all the sizes
    @Test
    fun testModelRecommendationOversized() {
        val deviceSpecs = DeviceSpecs("", "", 163.9, 6.45)
        val bestModels = phoneSpecsRepository.getBestModels(deviceSpecs)
        assertEquals("New model does not match", bestModels.first, TapModel.CORAL)
        assertEquals("Legacy model does not match", bestModels.second, TapModel.PIXEL4_XL)
    }

    //Test for a phone that sits exactly half way between sizes (it should round up)
    @Test
    fun testModelRecommendationRoundUp() {
        val deviceSpecs = DeviceSpecs("", "", 159.2, 6.27)
        val bestModels = phoneSpecsRepository.getBestModels(deviceSpecs)
        assertEquals("New model does not match", bestModels.first, TapModel.CROSSHATCH)
        assertEquals("Legacy model does not match", bestModels.second, TapModel.PIXEL3_XL)
    }

    //Test for a phone that's below all the sizes. Note that the legacy phone doesn't match the new.
    @Test
    fun testModelRecommendationUndersized() {
        val deviceSpecs = DeviceSpecs("", "", 140.0, 5.51)
        val bestModels = phoneSpecsRepository.getBestModels(deviceSpecs)
        assertEquals("New model does not match", bestModels.first, TapModel.REDFIN)
        assertEquals("Legacy model does not match", bestModels.second, TapModel.PIXEL4)
    }

}