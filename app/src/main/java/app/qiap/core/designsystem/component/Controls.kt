package app.qiap.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapShadow
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.qiapShadow
import app.qiap.core.designsystem.theme.qiapTween
import kotlin.math.abs

enum class ToggleAccent {
    /** Alarm on/off: Cinnabar is the alarm-on color (design.md §3). */
    Alarm,
    /** Every other setting. */
    Ink,
}

/** Square toggle (design.md §7: "square toggle"), 52×32, radius 10, sliding 24dp thumb. */
@Composable
fun QiapToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    accent: ToggleAccent = ToggleAccent.Ink,
) {
    val colors = QiapTheme.colors
    val on = if (accent == ToggleAccent.Alarm) colors.cinnabar else colors.ink
    val track by animateColorAsState(if (checked) on else colors.surfaceMuted, qiapTween(), label = "toggleTrack")
    val x by animateDpAsState(if (checked) 20.dp else 0.dp, qiapTween(), label = "toggleThumb")
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier
            .size(52.dp, 32.dp)
            .clip(shape)
            .background(track)
            .border(QiapSize.hairline, if (checked) Color.Transparent else colors.border, shape)
            .toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange)
            .semantics { this.contentDescription = contentDescription }
            .padding(4.dp),
    ) {
        Box(
            Modifier
                .offset(x = x)
                .size(24.dp)
                .qiapShadow(QiapShadow.soft, RoundedCornerShape(7.dp))
                .background(Color.White, RoundedCornerShape(7.dp)),
        )
    }
}

/** − value + with 40dp tiles and a big tabular number. */
@Composable
fun QiapStepper(
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        IconTile(QiapIcons.Minus, "Fewer", size = 40.dp, onClick = onDecrement)
        QiapText(
            "$value",
            style = QiapTheme.type.displayCompact.copy(fontSize = 32.sp, lineHeight = 32.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(52.dp),
        )
        IconTile(QiapIcons.Plus, "More", size = 40.dp, onClick = onIncrement)
    }
}

/** Ink-on-muted slider (volume). */
@Composable
fun QiapSlider(value: Float, onValueChange: (Float) -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    val colors = QiapTheme.colors
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        colors = SliderDefaults.colors(
            thumbColor = colors.ink,
            activeTrackColor = colors.ink,
            inactiveTrackColor = colors.surfaceMuted,
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
        ),
    )
}

/** Rounded search input (design.md §7, Library). */
@Composable
fun SearchPill(placeholder: String, modifier: Modifier = Modifier, onQueryChange: (String) -> Unit = {}) {
    val colors = QiapTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    Row(
        modifier
            .fillMaxWidth()
            .height(QiapSize.iconTile)
            .background(colors.surfaceMuted, QiapRadius.pill)
            .padding(horizontal = QiapSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QiapIcon(QiapIcons.Search, contentDescription = null, tint = colors.ink3)
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) QiapText(placeholder, style = QiapTheme.type.bodySmall, color = colors.ink3, maxLines = 1)
            BasicTextField(
                value = query,
                onValueChange = { query = it; onQueryChange(it) },
                singleLine = true,
                textStyle = QiapTheme.type.bodySmall.copy(color = colors.ink),
                cursorBrush = SolidColor(colors.ink),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = placeholder },
            )
        }
    }
}

private val WheelItem = 56.dp
private val WheelHeight = 192.dp

/**
 * Hour:minute wheel with center snapping and faded edges. Minutes step by [minuteStep].
 * Reports the centered values once scrolling settles.
 */
@Composable
fun TimeWheel(
    hour: Int,
    minute: Int,
    onChange: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier,
    minuteStep: Int = 5,
) {
    val colors = QiapTheme.colors
    val latest by rememberUpdatedState(onChange)
    var h by remember { mutableStateOf(hour) }
    var m by remember { mutableStateOf(minute) }
    Box(
        modifier.fillMaxWidth().height(WheelHeight).clip(QiapRadius.cardLarge).background(colors.surfaceMuted),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .padding(horizontal = QiapSpacing.sm)
                .fillMaxWidth()
                .height(WheelItem)
                .qiapShadow(QiapShadow.soft, QiapRadius.cardSmall)
                .background(colors.surface, QiapRadius.cardSmall)
                .border(QiapSize.hairline, colors.border, QiapRadius.cardSmall),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            WheelColumn(24, h) { h = it; latest(h, m) }
            QiapText(":", style = WheelText, modifier = Modifier.padding(bottom = 6.dp))
            WheelColumn(60 / minuteStep, m / minuteStep, label = { it * minuteStep }) { m = it * minuteStep; latest(h, m) }
        }
    }
}

private val WheelText @Composable get() = QiapTheme.type.displayCompact.copy(
    fontSize = 40.sp, lineHeight = 40.sp, letterSpacing = (-0.03).em, fontWeight = FontWeight.SemiBold,
)

@Composable
private fun WheelColumn(count: Int, selected: Int, label: (Int) -> Int = { it }, onSelected: (Int) -> Unit) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selected)
    val latest by rememberUpdatedState(onSelected)
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }.collect { scrolling ->
            if (scrolling) return@collect
            val info = state.layoutInfo
            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - center) }?.let { latest(it.index) }
        }
    }
    val pad = (WheelHeight - WheelItem) / 2
    LazyColumn(
        state = state,
        flingBehavior = rememberSnapFlingBehavior(state, SnapPosition.Center),
        contentPadding = PaddingValues(vertical = pad),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(96.dp)
            .height(WheelHeight)
            // Fade top/bottom with a DstIn mask; needs its own layer.
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(
                    Brush.verticalGradient(0f to Color.Transparent, 0.34f to Color.Black, 0.66f to Color.Black, 1f to Color.Transparent),
                    blendMode = BlendMode.DstIn,
                )
            },
    ) {
        items(count) { i ->
            Box(Modifier.height(WheelItem), contentAlignment = Alignment.Center) {
                QiapText(label(i).toString().padStart(2, '0'), style = WheelText)
            }
        }
    }
}
