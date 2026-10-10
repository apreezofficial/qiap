package app.qiap.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.component.SealGlyph
import app.qiap.core.designsystem.theme.DawnColors
import app.qiap.core.designsystem.theme.NightColors
import app.qiap.core.designsystem.theme.QiapShadow
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapThemeVariant
import app.qiap.core.designsystem.theme.RingingColors
import app.qiap.core.designsystem.theme.resolveColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignTokensTest {

    @Test
    fun spacingScaleIsExactlyTheSpec() {
        val spec = listOf(4, 8, 12, 16, 24, 32, 48, 64, 96).map { it.dp }
        assertEquals(spec, QiapSpacing.scale)
    }

    @Test
    fun shadowsMatchCss() {
        assertEquals(8.dp, QiapShadow.soft.offset.y)
        assertEquals(24.dp, QiapShadow.soft.blur)
        assertEquals(0.06f, QiapShadow.soft.color.alpha, 0.005f)
        assertEquals(6.dp, QiapShadow.strong.offset.y)
        assertEquals(16.dp, QiapShadow.strong.blur)
        assertEquals(0.25f, QiapShadow.strong.color.alpha, 0.005f)
    }

    @Test
    fun dawnFollowsSystemDarkButRingingAndNightAreFixed() {
        assertSame(DawnColors, resolveColors(QiapThemeVariant.Dawn, systemDark = false))
        assertSame(NightColors, resolveColors(QiapThemeVariant.Dawn, systemDark = true))
        assertSame(NightColors, resolveColors(QiapThemeVariant.Night, systemDark = false))
        assertSame(RingingColors, resolveColors(QiapThemeVariant.Ringing, systemDark = true))
    }

    @Test
    fun textContrastMeetsWcag() {
        // Body text: AA (4.5). Captions (ink3) carry real information (times, hints): ~AA too.
        for (c in listOf(DawnColors, NightColors)) {
            assertContrast(c.ink, c.bg, 7.0)
            assertContrast(c.ink2, c.bg, 4.5)
            assertContrast(c.ink3, c.bg, 4.0) // raised from 2.5 in the Phase 6 accessibility pass
            assertContrast(c.onInk, c.ink, 7.0)
        }
        // Giant ink time on Cinnabar.
        assertContrast(RingingColors.ink, RingingColors.bg, 4.5)
    }

    @Test
    fun activeTabTextOnSkyMeetsAA() {
        // Sky was #4A90E2 (white on it is ~3.3:1, below AA for 13sp text). Deepened to #2B74D6 in the
        // Phase 6 accessibility pass: ~4.6:1, so the 13sp active-tab label passes AA.
        assertContrast(DawnColors.onAccent, DawnColors.sky, 4.5)
        // Sky also serves as link/icon colour on the white page.
        assertContrast(DawnColors.sky, DawnColors.bg, 4.5)
    }

    @Test
    fun sealGlyphUsesAbsoluteCommandsInsideViewBox() {
        val commands = Regex("[A-Za-z]").findAll(SealGlyph.PATH).map { it.value }.toSet()
        assertTrue("relative commands found: $commands", commands.all { it[0].isUpperCase() })
        val numbers = Regex("-?\\d+(\\.\\d+)?").findAll(SealGlyph.PATH).map { it.value.toFloat() }.toList()
        assertTrue(numbers.isNotEmpty())
        assertTrue(numbers.all { it in 0f..SealGlyph.VIEWBOX })
    }

    private fun assertContrast(fg: Color, bg: Color, min: Double) {
        val a = fg.luminance() + 0.05
        val b = bg.luminance() + 0.05
        val ratio = maxOf(a, b) / minOf(a, b)
        assertTrue("contrast %.2f < %.1f for %s on %s".format(ratio, min, fg, bg), ratio >= min)
    }
}
