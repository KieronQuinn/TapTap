package com.kieronquinn.app.taptap.repositories.update

import androidx.test.platform.app.InstrumentationRegistry
import com.kieronquinn.app.taptap.components.settings.TapTapSettingsImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateRepositoryTests {

    private val context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    private val settings = TapTapSettingsImpl(context)
    private val updateRepository = UpdateRepositoryImpl(settings)

    @Test
    fun testUpdateFound() {
        runBlocking {
            val internetEnabledBefore = settings.internetAllowed.get()
            settings.internetAllowed.set(true)
            assertEquals("Update not found when one expected", updateRepository.getUpdate("1.0") != null, true)
            settings.internetAllowed.set(internetEnabledBefore)
        }
    }

    @Test
    fun testUpdateNotFound() {
        runBlocking {
            val internetEnabledBefore = settings.internetAllowed.get()
            settings.internetAllowed.set(true)
            assertEquals("Update found when one was not expected", updateRepository.getUpdate("0.10.1_beta") == null, true)
            settings.internetAllowed.set(internetEnabledBefore)
        }
    }

}