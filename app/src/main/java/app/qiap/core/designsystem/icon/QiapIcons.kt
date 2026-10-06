package app.qiap.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The app's only icon set: Lucide outlines (ISC license), stroke 1.75, hand-copied so we ship
 * just the icons we use instead of an icon library. Add new icons here, never inline.
 */
object QiapIcons {
    val Alarm: ImageVector by lazy {
        lucide(
            "Alarm",
            circle(12f, 13f, 8f),
            "M12 9v4l2 2", "M5 3 2 6", "M22 6l-3-3", "M6.38 18.7 4 21", "M17.64 18.67 20 21",
        )
    }
    val Library: ImageVector by lazy {
        lucide(
            "Library",
            rect(3f, 3f, 7f, 7f, 1f), rect(14f, 3f, 7f, 7f, 1f),
            rect(14f, 14f, 7f, 7f, 1f), rect(3f, 14f, 7f, 7f, 1f),
        )
    }
    val History: ImageVector by lazy {
        lucide(
            "History",
            rect(3f, 4f, 18f, 18f, 2f), "M16 2v4", "M8 2v4", "M3 10h18", "M9 16l2 2 4-4",
        )
    }
    val Settings: ImageVector by lazy {
        lucide("Settings", "M20 7h-9", "M14 17H5", circle(17f, 17f, 3f), circle(7f, 7f, 3f))
    }
    val Plus: ImageVector by lazy { lucide("Plus", "M5 12h14", "M12 5v14") }
    val Play: ImageVector by lazy { lucide("Play", "M6 3l14 9-14 9z") }
    val Star: ImageVector by lazy {
        lucide(
            "Star",
            "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01z",
        )
    }
    val Check: ImageVector by lazy { lucide("Check", "M20 6 9 17l-5-5") }
    val ChevronRight: ImageVector by lazy { lucide("ChevronRight", "M9 18l6-6-6-6") }
    val ChevronLeft: ImageVector by lazy { lucide("ChevronLeft", "M15 18l-6-6 6-6") }
    val Palette: ImageVector by lazy {
        lucide(
            "Palette",
            circle(12f, 12f, 10f), circle(13.5f, 6.5f, 0.5f), circle(17.5f, 10.5f, 0.5f),
            circle(8.5f, 7.5f, 0.5f), circle(6.5f, 12.5f, 0.5f),
        )
    }

    /** Brand glyph: half-sun rising over a horizon (same geometry as the web logo, 32 grid). */
    val SunriseGlyph: ImageVector by lazy {
        ImageVector.Builder("SunriseGlyph", 24.dp, 24.dp, 32f, 32f).apply {
            stroked("M5 21a11 11 0 0 1 22 0", 3.2f)
            stroked("M3 26.5h26", 3.2f)
            stroked("M16 4v3.2M6.2 8.6l2.2 2.2M25.8 8.6l-2.2 2.2", 2.4f)
        }.build()
    }

    private fun lucide(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach { stroked(it, 1.75f) }
        }.build()

    private fun ImageVector.Builder.stroked(d: String, width: Float) {
        addPath(
            pathData = addPathNodes(d),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }

    private fun circle(cx: Float, cy: Float, r: Float) =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun rect(x: Float, y: Float, w: Float, h: Float, rx: Float) =
        "M${x + rx} ${y}h${w - 2 * rx}a$rx $rx 0 0 1 $rx ${rx}v${h - 2 * rx}" +
            "a$rx $rx 0 0 1 ${-rx} ${rx}h${-(w - 2 * rx)}a$rx $rx 0 0 1 ${-rx} ${-rx}" +
            "v${-(h - 2 * rx)}a$rx $rx 0 0 1 $rx ${-rx}z"
}
