package app.qiap.core.designsystem.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.offset

/**
 * Lets a horizontally scrolling row reach the screen edges inside a padded column, so items
 * scroll under the gutter instead of being clipped at it. Pair with matching content padding.
 */
fun Modifier.bleedHorizontal(amount: Dp): Modifier = layout { measurable, constraints ->
    val px = amount.roundToPx()
    val placeable = measurable.measure(constraints.offset(horizontal = 2 * px))
    layout((placeable.width - 2 * px).coerceAtLeast(0), placeable.height) { placeable.place(-px, 0) }
}

/**
 * Draws one of the two [QiapShadow] levels with CSS semantics (offset + blur + color),
 * so Android matches the web exactly. Never use on the workout screen over the camera.
 */
fun Modifier.qiapShadow(spec: QiapShadowSpec, shape: Shape): Modifier = dropShadow(
    shape = shape,
    shadow = Shadow(
        radius = spec.blur,
        color = spec.color,
        offset = spec.offset,
    ),
)
