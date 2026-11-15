package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.controller.PhoneController
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PhoneControllerTest {

    @Test
    fun testParseTorchCommand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = PhoneController.parseAndExecuteCommand(context, "माया टॉर्च ऑन करो")
        assertNotNull(result)
        assertTrue(result?.responseText?.contains("टॉर्च") == true || result?.responseText?.contains("फ्लैशलाइट") == true)
    }

    @Test
    fun testParseBatteryCommand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = PhoneController.parseAndExecuteCommand(context, "बैटरी कितनी है")
        assertNotNull(result)
        assertTrue(result?.responseText?.contains("बैटरी") == true)
    }

    @Test
    fun testParseVolumeCommand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = PhoneController.parseAndExecuteCommand(context, "वॉल्यूम बढ़ाओ")
        assertNotNull(result)
        assertTrue(result?.responseText?.contains("वॉल्यूम बढ़ा") == true)
    }

    @Test
    fun testParseAppLaunchCommand() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val result = PhoneController.parseAndExecuteCommand(context, "यूट्यूब खोलो")
        assertNotNull(result)
        assertTrue(result?.responseText?.contains("YouTube") == true || result?.responseText?.contains("यूट्यूब") == true)
    }
}
