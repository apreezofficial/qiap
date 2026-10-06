package app.qiap.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme

/**
 * Standard screen frame: theme background, safe-area insets, 16dp gutters, optional back tile +
 * title row with [actions], scrolling content and a floating [bottomBar] slot (sticky CTA).
 * [bottomClearance] reserves space so the last item can scroll above floating bars.
 * [wash] hangs the sky wash from the top edge (Home, Success).
 */
@Composable
fun QiapScreen(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    background: Color = QiapTheme.colors.bg,
    wash: Boolean = false,
    bottomClearance: Dp = QiapSpacing.lg,
    bottomBar: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = QiapTheme.colors
    CompositionLocalProvider(LocalContentColor provides colors.ink) {
        Box(
            modifier
                .fillMaxSize()
                .background(background)
                .let { if (wash) it.skyWash(fromTop = true) else it },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = QiapSpacing.md),
                verticalArrangement = Arrangement.spacedBy(QiapSpacing.md),
            ) {
                if (title != null || onBack != null || actions != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = QiapSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (onBack != null) IconTile(QiapIcons.ChevronLeft, "Back", onClick = onBack)
                        if (title != null) QiapText(title, style = QiapTheme.type.h3, modifier = Modifier.weight(1f))
                        else Spacer(Modifier.weight(1f))
                        actions?.invoke(this)
                    }
                }
                content()
                Spacer(Modifier.height(bottomClearance))
            }
            if (bottomBar != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        // Fade the content out under the sticky bar instead of a hard edge.
                        .background(Brush.verticalGradient(0f to Color.Transparent, 0.35f to background))
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(start = QiapSpacing.md, end = QiapSpacing.md, top = QiapSpacing.lg, bottom = QiapSpacing.md),
                    content = bottomBar,
                )
            }
        }
    }
}
