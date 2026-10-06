package app.qiap.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/** Spacing scale (design.md §5). These nine values are the only ones allowed. */
object QiapSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
    val section = 96.dp

    val scale: List<Dp> = listOf(xxs, xs, sm, md, lg, xl, xxl, xxxl, section)
}

/** Corner radii (design.md §5–§6). */
object QiapRadius {
    /** Chips, pills, inputs, nav. */
    val pill = CircleShape
    /** IconTile. */
    val tile = RoundedCornerShape(12.dp)
    val cardSmall = RoundedCornerShape(16.dp)
    val cardLarge = RoundedCornerShape(24.dp)
    /** Phone-showcase containers. */
    val showcase = RoundedCornerShape(32.dp)
    /** SealStamp corner: 22% of the side, matching the web seal and the app icon. */
    val seal = RoundedCornerShape(22)
}

/** A CSS-style drop shadow: `offset blur color`. */
data class QiapShadowSpec(val offset: DpOffset, val blur: Dp, val color: Color)

/** The only two shadow levels (design.md §5). */
object QiapShadow {
    /** `0 8px 24px rgba(17,17,20,0.06)`: cards, nav pill. */
    val soft = QiapShadowSpec(DpOffset(0.dp, 8.dp), 24.dp, Color(0x0F111114))
    /** `0 6px 16px rgba(17,17,20,0.25)`: the dark primary pill only. */
    val strong = QiapShadowSpec(DpOffset(0.dp, 6.dp), 16.dp, Color(0x40111114))
}

/** One duration and one curve everywhere (design.md §10). */
object QiapMotion {
    const val DURATION_MS = 200
    val easing: Easing = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
    const val SEAL_STAMP_MS = 400
    const val SUN_PULSE_MS = 3000
}

/** Fixed component sizes from design.md §6. */
object QiapSize {
    val pillHeight = 52.dp
    val pillHorizontalPadding = 24.dp
    val iconTile = 48.dp
    val navItem = 48.dp
    val icon = 20.dp
    val hairline = 1.dp
}
