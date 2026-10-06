package app.qiap.feature.workout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.FormFeedbackPill
import app.qiap.core.designsystem.component.FormState
import app.qiap.core.designsystem.component.QiapProgressBar
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.component.RecordingDot
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.pictogram.Pictograms
import app.qiap.core.designsystem.theme.QiapMotion
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import kotlinx.coroutines.delay

private const val TARGET = 12

/**
 * Night theme over the camera feed (design.md §7). Until the pose pipeline exists, a looping
 * skeleton stands in for the camera and a timer counts one rep per cycle. Everything drawn over
 * the "camera" is opaque: no glass, no blur.
 */
@Composable
fun WorkoutScreen(onComplete: () -> Unit) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    var reps by rememberSaveable { mutableIntStateOf(0) }
    var seconds by rememberSaveable { mutableIntStateOf(0) }
    val done by rememberUpdatedState(onComplete)

    LaunchedEffect(Unit) {
        while (reps < TARGET) {
            delay(Pictograms.Squat.periodMs.toLong())
            if (reps < TARGET) reps++
        }
        delay(500)
        done()
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }
    val offForm = reps % 4 == 2

    // Rep counter pops and flashes jade on every new rep.
    val bump = remember { Animatable(0f) }
    LaunchedEffect(reps) {
        if (reps == 0) return@LaunchedEffect
        bump.snapTo(1f)
        bump.animateTo(0f, tween(350, easing = QiapMotion.easing))
    }

    Box(Modifier.fillMaxSize().cameraStandIn()) {
        Pictogram(
            Pictograms.Squat,
            Modifier.align(Alignment.TopCenter).padding(top = 120.dp).size(300.dp),
            color = colors.ink,
            showFloor = false,
            showJoints = true,
            offForm = offForm,
        )

        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(QiapSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .background(colors.surface, QiapRadius.pill)
                    .padding(horizontal = QiapSpacing.sm, vertical = QiapSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RecordingDot()
                QiapText(
                    "Proof · ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}",
                    style = type.numeric.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
                )
            }
            Chip("Squat", leading = { Pictogram(Pictograms.Squat, Modifier.size(18.dp), showFloor = false) })
        }

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(QiapSpacing.md),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            FormFeedbackPill(
                if (offForm) "Go a little lower" else "Good depth",
                if (offForm) FormState.Off else FormState.Good,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                QiapText(
                    "$reps",
                    style = type.display.copy(fontSize = 128.sp, lineHeight = 116.sp, letterSpacing = (-0.05).em),
                    color = lerp(colors.ink, colors.jade, bump.value),
                    modifier = Modifier.graphicsLayer {
                        val s = 1f + 0.18f * bump.value
                        scaleX = s
                        scaleY = s
                        transformOrigin = TransformOrigin(0f, 1f)
                    },
                )
                QiapText(
                    "/ $TARGET",
                    style = type.displayCompact.copy(fontSize = 28.sp, lineHeight = 28.sp),
                    color = colors.ink3,
                    modifier = Modifier.padding(bottom = 14.dp),
                )
            }
            QiapProgressBar(reps / TARGET.toFloat())
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(QiapSpacing.xs)) {
                QiapText(
                    "Counting on-device. Nothing leaves your phone.",
                    style = type.caption,
                    color = colors.ink3,
                    modifier = Modifier.weight(1f),
                )
                Chip("+1 rep", onClick = { if (reps < TARGET) reps++ })
            }
        }
    }
}

/** Dim room + floor line where the CameraX preview will go. Opaque, drawn once per size. */
private fun Modifier.cameraStandIn(): Modifier = drawWithCache {
    val glow = Brush.radialGradient(
        listOf(Color(0xFF2A2723), Color.Transparent),
        center = Offset(size.width / 2f, size.height * 0.3f),
        radius = size.width * 0.8f,
    )
    val room = Brush.verticalGradient(
        0f to Color(0xFF1B1A18), 0.62f to Color(0xFF0D0D0F), 0.621f to Color(0xFF141312), 1f to Color(0xFF0D0D0F),
    )
    onDrawBehind {
        drawRect(room)
        drawRect(glow)
    }
}
