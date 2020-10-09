package com.kieronquinn.app.taptap

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kieronquinn.app.taptap.models.TfModel
import com.kieronquinn.app.taptap.models.getDefaultTfModel
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenSizeTests {

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun testSmallScreen(){
        val screenSize = 5.0
        val tfModel = context.getDefaultTfModel(screenSize)
        assertTrue("Small size not handled correctly", tfModel == TfModel.PIXEL4)
    }

    @Test
    fun testMediumScreen(){
        val screenSize = 6.5
        val tfModel = context.getDefaultTfModel(screenSize)
        assertTrue("Medium size not handled correctly", tfModel == TfModel.PIXEL4XL)
    }

    @Test
    fun testLargeScreen(){
        val screenSize = 7.0
        val tfModel = context.getDefaultTfModel(screenSize)
        assertTrue("Large size not handled correctly", tfModel == TfModel.PIXEL4XL)
    }

    @Test
    fun testMediumLowerBound(){
        val screenSize = 6.24
        val tfModel = context.getDefaultTfModel(screenSize)
        assertTrue("Medium lower bound size not handled correctly", tfModel == TfModel.PIXEL4XL)
    }

    @Test
    fun testSmallUpperBound(){
        val screenSize = 6.23
        val tfModel = context.getDefaultTfModel(screenSize)
        assertTrue("Small upper bound size not handled correctly", tfModel == TfModel.PIXEL4)
    }

}