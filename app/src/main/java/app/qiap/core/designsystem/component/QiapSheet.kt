package app.qiap.core.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapTween

/**
 * Bottom sheet over a dimmed screen (exercise detail). Call inside a full-screen Box, after the
 * content it covers. Back and scrim taps dismiss. Flat scrim: no blur (design.md §5).
 */
@Composable
fun BoxScope.QiapSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = QiapTheme.colors
    BackHandler(enabled = visible, onBack = onDismiss)
    AnimatedVisibility(visible, enter = fadeIn(qiapTween()), exit = fadeOut(qiapTween())) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x59111114))
                .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        )
    }
    AnimatedVisibility(
        visible,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(qiapTween(300)) { it },
        exit = slideOutVertically(qiapTween()) { it },
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.ink) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.bg, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    // Swallow taps so they don't reach the scrim.
                    .clickable(remember { MutableInteractionSource() }, indication = null) {}
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = QiapSpacing.md, end = QiapSpacing.md, top = QiapSpacing.sm, bottom = QiapSpacing.md),
                verticalArrangement = Arrangement.spacedBy(QiapSpacing.md),
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(40.dp, 4.dp)
                        .background(colors.border, QiapRadius.pill),
                )
                content()
            }
        }
    }
}
