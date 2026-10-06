package app.qiap.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapShadow
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapShadow
import app.qiap.core.designsystem.theme.qiapTween

enum class PillButtonStyle {
    /** Ink fill, strong shadow. One per screen: the action. */
    Primary,
    /** Flat glass (translucent fill + faint sky gradient + hairline). Never blurred. */
    Secondary,
    /** White fill, ink text. Only on a Cinnabar background (Ringing). */
    OnAccent,
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillButtonStyle = PillButtonStyle.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = QiapTheme.colors
    val shape = QiapRadius.pill
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, qiapTween(), label = "pillScale")

    val content: Color
    val surface: Modifier
    when (style) {
        PillButtonStyle.Primary -> {
            content = colors.onInk
            surface = Modifier
                .qiapShadow(QiapShadow.strong, shape)
                .background(colors.ink, shape)
        }
        PillButtonStyle.Secondary -> {
            content = colors.ink
            surface = Modifier
                .background(colors.glass, shape)
                .background(
                    Brush.verticalGradient(listOf(Color.Transparent, colors.bgWash.copy(alpha = 0.6f))),
                    shape,
                )
                .border(QiapSize.hairline, colors.border, shape)
        }
        PillButtonStyle.OnAccent -> {
            content = colors.ink
            surface = Modifier
                .qiapShadow(QiapShadow.soft, shape)
                .background(colors.surface, shape)
        }
    }

    CompositionLocalProvider(LocalContentColor provides content) {
        Row(
            modifier = modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .alpha(if (enabled) 1f else 0.4f)
                .then(surface)
                .clip(shape)
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(color = content),
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .heightIn(min = QiapSize.pillHeight)
                .padding(horizontal = QiapSize.pillHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) QiapIcon(leadingIcon, contentDescription = null)
            QiapText(text, style = QiapTheme.type.label.copy(fontSize = QiapTheme.type.body.fontSize))
        }
    }
}
