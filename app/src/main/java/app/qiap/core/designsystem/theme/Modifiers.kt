package app.qiap.core.designsystem.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.draw.dropShadow

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
