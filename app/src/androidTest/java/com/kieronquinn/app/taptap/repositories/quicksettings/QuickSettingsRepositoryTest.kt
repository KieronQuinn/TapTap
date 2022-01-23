package com.kieronquinn.app.taptap.repositories.quicksettings

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Test

class QuickSettingsRepositoryTest {

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    private val quickSettingsRepository: QuickSettingsRepository by lazy {
        QuickSettingsRepositoryImpl(context)
    }

    //Note: This test assumes you have at least one custom tile set.
    @Test
    fun testCustomTiles() {
        runBlocking {
            val tiles = quickSettingsRepository.getQuickSettings()
            assertEquals("No custom tiles returned", tiles.isNotEmpty(), true)
        }
    }

}