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

    val Minus: ImageVector by lazy { lucide("Minus", "M5 12h14") }
    val Search: ImageVector by lazy { lucide("Search", circle(11f, 11f, 8f), "M21 21l-4.3-4.3") }
    val Bell: ImageVector by lazy {
        lucide("Bell", "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9", "M10.3 21a1.94 1.94 0 0 0 3.4 0")
    }
    val Camera: ImageVector by lazy {
        lucide(
            "Camera",
            "M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3z",
            circle(12f, 13f, 3f),
        )
    }
    val Zap: ImageVector by lazy { lucide("Zap", "M13 2 3 14h9l-1 8 10-12h-9z") }
    val Battery: ImageVector by lazy { lucide("Battery", rect(2f, 7f, 16f, 10f, 2f), "M22 11v2", "M6 11v2") }
    val Layers: ImageVector by lazy {
        lucide("Layers", "M12 2l10 5-10 5L2 7z", "M2 17l10 5 10-5", "M2 12l10 5 10-5")
    }
    val Music: ImageVector by lazy {
        lucide("Music", "M9 18V5l12-2v13", circle(6f, 18f, 3f), circle(18f, 16f, 3f))
    }
    val Video: ImageVector by lazy {
        lucide("Video", "M16 13l5.2 3.5a.5.5 0 0 0 .8-.4V7.9a.5.5 0 0 0-.8-.4L16 11", rect(2f, 6f, 14f, 12f, 2f))
    }
    val Close: ImageVector by lazy { lucide("Close", "M18 6 6 18", "M6 6l12 12") }
    val Share: ImageVector by lazy {
        lucide("Share", circle(18f, 5f, 3f), circle(6f, 12f, 3f), circle(18f, 19f, 3f), "M8.59 13.51l6.83 3.98", "M15.41 6.51l-6.82 3.98")
    }
    val Timer: ImageVector by lazy { lucide("Timer", circle(12f, 14f, 8f), "M10 2h4", "M12 14l3-3") }
    val Flame: ImageVector by lazy {
        lucide(
            "Flame",
            "M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.07-2.14-.22-4.05 2-6 .5 2.5 2 4.9 4 6.5 " +
                "2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.15.43-2.29 1-3a2.5 2.5 0 0 0 2.5 2.5z",
        )
    }
    val Shield: ImageVector by lazy {
        lucide(
            "Shield",
            "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1" +
                "c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",
            "M9 12l2 2 4-4",
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
