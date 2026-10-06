package app.qiap.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.qiap.R

/** Inter: headings. Bundled Latin subset, weights 400/500/600 only (design.md §4). */
val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)

/** Plus Jakarta Sans: in-app labels and the big counters. */
val JakartaFamily = FontFamily(
    Font(R.font.jakarta_regular, FontWeight.Normal),
    Font(R.font.jakarta_medium, FontWeight.Medium),
    Font(R.font.jakarta_semibold, FontWeight.SemiBold),
)

/** Tabular numerals so times and counters never jitter. */
internal const val TNUM = "tnum"

/**
 * Android type scale from design.md §4. Colors are left unspecified; components pick the token
 * (ink for titles, ink2 for body, ink3 for captions).
 */
@Immutable
data class QiapTypography(
    /** Alarm time, rep count. */
    val display: TextStyle,
    /** Alarm time on a Home card. Not in the spec table; 56sp keeps two cards on a 360dp screen. */
    val displayCompact: TextStyle,
    /** Section heading; use [app.qiap.core.designsystem.component.TwoToneHeadline] for the gray phrase. */
    val h2: TextStyle,
    /** Card title. */
    val h3: TextStyle,
    val body: TextStyle,
    /** Chips, pills, tabs. */
    val label: TextStyle,
    val caption: TextStyle,
    /** Inline numbers (countdowns, stats). Body size with tabular numerals. */
    val numeric: TextStyle,
)

val DefaultTypography = QiapTypography(
    display = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 96.sp,
        lineHeight = 100.sp,
        letterSpacing = (-0.03).em,
        fontFeatureSettings = TNUM,
    ),
    displayCompact = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 56.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.03).em,
        fontFeatureSettings = TNUM,
    ),
    h2 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.025).em,
    ),
    h3 = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.02).em,
    ),
    body = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    label = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
    caption = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    numeric = TextStyle(
        fontFamily = JakartaFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFeatureSettings = TNUM,
    ),
)

/** Same style with tabular numerals, for headings that show numbers ("in 7h 32m"). */
fun TextStyle.tabular(): TextStyle = copy(fontFeatureSettings = TNUM)
