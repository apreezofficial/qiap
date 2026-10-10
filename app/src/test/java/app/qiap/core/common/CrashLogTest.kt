package app.qiap.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashLogTest {

    @Test
    fun entryNamesTheBuildThePhoneAndTheCause() {
        val text = CrashLog.format(123L, "Tecno KG5", 33, "0.1.0", "main", IllegalStateException("boom"))
        assertTrue(text.contains("Qiap 0.1.0"))
        assertTrue(text.contains("Tecno KG5"))
        assertTrue(text.contains("API 33"))
        assertTrue(text.contains("IllegalStateException: boom"))
    }

    @Test
    fun theLogKeepsTheNewestTextWhenItGrowsTooBig() {
        val old = "a".repeat(100)
        val entry = "NEW ENTRY"
        val out = CrashLog.trim(old, entry, maxChars = 50)
        assertEquals(50, out.length)
        assertTrue(out.endsWith(entry))
    }

    @Test
    fun aSmallLogJustGrows() {
        assertEquals("oldnew", CrashLog.trim("old", "new"))
    }
}
