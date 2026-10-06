package app.qiap.feature.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.CardTone
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.FloatingNavPill
import app.qiap.core.designsystem.component.FormFeedbackPill
import app.qiap.core.designsystem.component.FormState
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.NavPillItem
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapIcon
import app.qiap.core.designsystem.component.QiapProgressBar
import app.qiap.core.designsystem.component.QiapScreen
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.QiapWordmark
import app.qiap.core.designsystem.component.SealStamp
import app.qiap.core.designsystem.component.SealState
import app.qiap.core.designsystem.component.SectionBadge
import app.qiap.core.designsystem.component.SegmentedTabs
import app.qiap.core.designsystem.component.TwoToneHeadline
import app.qiap.core.designsystem.component.skyWash
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.theme.QiapColors
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapShadow
import app.qiap.core.designsystem.theme.QiapSize
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.core.designsystem.theme.QiapThemeVariant
import app.qiap.core.designsystem.theme.qiapShadow

/**
 * Debug-only: every token and component on one scroll, in Dawn, Night or Ringing, for eyeball
 * consistency checks. Reached from Settings in debug builds; R8 drops it from release.
 */
@Composable
fun DesignGalleryScreen(onBack: () -> Unit) {
    var variant by rememberSaveable { mutableIntStateOf(0) }
    val themes = QiapThemeVariant.entries
    QiapTheme(themes[variant], systemDark = false) {
        QiapScreen(title = "Design gallery", onBack = onBack) {
            SegmentedTabs(themes.map { it.name }, variant, { variant = it }, fillWidth = true)
            ColorsSection()
            TypeSection()
            SpacingSection()
            ShapeSection()
            ButtonsSection()
            ChipsAndTabsSection()
            CardsSection()
            SealSection()
            WorkoutSection()
            NavSection()
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
        Box(Modifier.padding(top = QiapSpacing.lg)) { SectionBadge(title) }
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColorsSection() = Section("Color tokens") {
    val c = QiapTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
    ) {
        c.swatches().forEach { (name, color) -> Swatch(name, color) }
    }
}

private fun QiapColors.swatches(): List<Pair<String, Color>> = listOf(
    "bg" to bg, "bgWash" to bgWash, "bgWash2" to bgWash2, "surface" to surface,
    "surfaceMuted" to surfaceMuted, "border" to border, "ink" to ink, "ink2" to ink2,
    "ink3" to ink3, "onInk" to onInk, "onAccent" to onAccent, "sky" to sky,
    "skyStrong" to skyStrong, "cinnabar" to cinnabar, "jade" to jade, "saffron" to saffron,
    "crimson" to crimson, "glass" to glass,
)

@Composable
private fun Swatch(name: String, color: Color) {
    val c = QiapTheme.colors
    Column(Modifier.width(96.dp), verticalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(QiapSpacing.xxl)
                .background(color, QiapRadius.tile)
                .border(QiapSize.hairline, c.border, QiapRadius.tile),
        )
        QiapText(name, style = QiapTheme.type.label)
        QiapText(
            "#" + Integer.toHexString(color.toArgb()).uppercase().padStart(8, '0'),
            style = QiapTheme.type.caption,
            color = c.ink3,
        )
    }
}

@Composable
private fun TypeSection() = Section("Type scale") {
    val t = QiapTheme.type
    val c = QiapTheme.colors
    val rows: List<Pair<String, TextStyle>> = listOf(
        "display 96" to t.display, "displayCompact 56" to t.displayCompact, "h2 28" to t.h2,
        "h3 20" to t.h3, "body 16" to t.body, "label 13" to t.label, "caption 12" to t.caption,
        "numeric 16" to t.numeric,
    )
    rows.forEach { (name, style) ->
        Column {
            QiapText(name, style = t.caption, color = c.ink3)
            QiapText(if (style.fontFeatureSettings != null) "06:30 1:11" else "Your bed lost.", style = style, maxLines = 1)
        }
    }
    TwoToneHeadline("Everything you need to", "actually get up.")
    QiapText("Tabular check: these must not jitter", style = t.caption, color = c.ink3)
    Column {
        listOf("11:11", "08:08", "10:01").forEach { QiapText(it, style = t.h3.copy(fontFeatureSettings = "tnum")) }
    }
}

@Composable
private fun SpacingSection() = Section("Spacing (only these)") {
    val c = QiapTheme.colors
    QiapSpacing.scale.forEach { dp ->
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
            QiapText("${dp.value.toInt()}", style = QiapTheme.type.numeric, modifier = Modifier.width(QiapSpacing.xl))
            Box(Modifier.width(dp).height(QiapSpacing.sm).background(c.sky, QiapRadius.pill))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShapeSection() = Section("Radius & shadow") {
    val c = QiapTheme.colors
    val shapes: List<Pair<String, Shape>> = listOf(
        "tile 12" to QiapRadius.tile, "card 16" to QiapRadius.cardSmall,
        "card 24" to QiapRadius.cardLarge, "showcase 32" to QiapRadius.showcase, "pill" to QiapRadius.pill,
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.md),
        verticalArrangement = Arrangement.spacedBy(QiapSpacing.md),
    ) {
        shapes.forEach { (name, shape) ->
            Column(verticalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
                Box(Modifier.size(QiapSpacing.xxxl).background(c.surfaceMuted, shape).border(QiapSize.hairline, c.border, shape))
                QiapText(name, style = QiapTheme.type.caption, color = c.ink3)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
            Box(Modifier.size(QiapSpacing.xxxl).qiapShadow(QiapShadow.soft, QiapRadius.cardSmall).background(c.surface, QiapRadius.cardSmall))
            QiapText("shadow soft", style = QiapTheme.type.caption, color = c.ink3)
        }
        Column(verticalArrangement = Arrangement.spacedBy(QiapSpacing.xxs)) {
            Box(Modifier.size(QiapSpacing.xxxl).qiapShadow(QiapShadow.strong, QiapRadius.pill).background(c.ink, QiapRadius.pill))
            QiapText("shadow strong", style = QiapTheme.type.caption, color = c.ink3)
        }
    }
    Box(
        Modifier.fillMaxWidth().height(120.dp).border(QiapSize.hairline, c.border, QiapRadius.cardLarge).skyWash(),
        contentAlignment = Alignment.Center,
    ) { QiapText("Sky wash", style = QiapTheme.type.label, color = c.ink2) }
}

@Composable
private fun ButtonsSection() = Section("Pill buttons & icon tiles") {
    PillButton("Download for Android", onClick = {}, leadingIcon = QiapIcons.Plus)
    PillButton("Watch demo", onClick = {}, style = PillButtonStyle.Secondary, leadingIcon = QiapIcons.Play)
    PillButton("Disabled", onClick = {}, enabled = false)
    Box(Modifier.fillMaxWidth().background(QiapTheme.colors.cinnabar, QiapRadius.cardLarge).padding(QiapSpacing.lg)) {
        PillButton("Start workout", onClick = {}, style = PillButtonStyle.OnAccent, modifier = Modifier.fillMaxWidth())
    }
    Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
        listOf(
            QiapIcons.Alarm, QiapIcons.Library, QiapIcons.History, QiapIcons.Settings,
            QiapIcons.Star, QiapIcons.Palette,
        ).forEach { IconTile(it, null, onClick = {}) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
        QiapWordmark()
        listOf(QiapIcons.Plus, QiapIcons.Play, QiapIcons.Check, QiapIcons.ChevronLeft, QiapIcons.ChevronRight)
            .forEach { QiapIcon(it, null) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsAndTabsSection() = Section("Chips & tabs") {
    var on by remember { mutableIntStateOf(1) }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
    ) {
        listOf("Mon", "Tue", "Wed").forEachIndexed { i, d -> Chip(d, selected = on == i, onClick = { on = i }) }
        Chip("52 exercises", leadingIcon = QiapIcons.Library)
        Chip("Works offline", leadingIcon = QiapIcons.Check)
    }
    var tab by remember { mutableIntStateOf(0) }
    SegmentedTabs(listOf("All", "Lower", "Upper", "Core", "Cardio", "Mobility"), tab, { tab = it })
    var tab2 by remember { mutableIntStateOf(0) }
    SegmentedTabs(listOf("Monthly", "Yearly"), tab2, { tab2 = it }, fillWidth = true)
}

@Composable
private fun CardsSection() = Section("Cards") {
    QiapCard {
        QiapText("Large card", style = QiapTheme.type.h3)
        QiapText("Radius 24, padding 24, hairline + soft shadow.", color = QiapTheme.colors.ink2)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
        QiapCard(Modifier.weight(1f), size = CardSize.Small, tone = CardTone.Muted) {
            QiapText("Muted", style = QiapTheme.type.h3)
        }
        QiapCard(Modifier.weight(1f), size = CardSize.Small, tone = CardTone.Sky) {
            QiapText("Sky", style = QiapTheme.type.h3)
        }
    }
}

@Composable
private fun SealSection() = Section("Seal stamp") {
    var replay by remember { mutableIntStateOf(0) }
    Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
        key(replay) { SealStamp(size = 96.dp, stampIn = replay > 0) }
        SealStamp(size = 64.dp, state = SealState.Fallback)
        SealStamp(size = 64.dp, state = SealState.Missed)
    }
    PillButton("Replay stamp", onClick = { replay++ }, style = PillButtonStyle.Secondary)
}

@Composable
private fun WorkoutSection() = Section("Workout overlays") {
    QiapTheme(QiapThemeVariant.Night) {
        Column(
            Modifier.fillMaxWidth().background(QiapTheme.colors.bg, QiapRadius.cardLarge).padding(QiapSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm),
        ) {
            FormFeedbackPill("Good depth", FormState.Good)
            FormFeedbackPill("Go lower", FormState.Off)
            QiapText("7", style = QiapTheme.type.display, color = QiapTheme.colors.ink)
            QiapProgressBar(7 / 12f)
        }
    }
}

@Composable
private fun NavSection() = Section("Floating nav") {
    var sel by remember { mutableIntStateOf(0) }
    FloatingNavPill(
        items = listOf(
            NavPillItem(QiapIcons.Alarm, "Alarms"),
            NavPillItem(QiapIcons.Library, "Library"),
            NavPillItem(QiapIcons.History, "History"),
        ),
        selectedIndex = sel,
        onSelect = { sel = it },
    )
}
