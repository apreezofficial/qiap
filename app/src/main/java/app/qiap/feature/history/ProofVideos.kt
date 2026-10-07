package app.qiap.feature.history

import android.content.Intent
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import app.qiap.alarm.HistoryEntry
import app.qiap.camera.ProofStore
import app.qiap.core.designsystem.component.CardSize
import app.qiap.core.designsystem.component.Chip
import app.qiap.core.designsystem.component.IconTile
import app.qiap.core.designsystem.component.PillButton
import app.qiap.core.designsystem.component.PillButtonStyle
import app.qiap.core.designsystem.component.QiapCard
import app.qiap.core.designsystem.component.QiapText
import app.qiap.core.designsystem.icon.QiapIcons
import app.qiap.core.designsystem.pictogram.Pictogram
import app.qiap.core.designsystem.theme.QiapRadius
import app.qiap.core.designsystem.theme.QiapSpacing
import app.qiap.core.designsystem.theme.QiapTheme
import app.qiap.exercise.ExerciseCatalog
import app.qiap.feature.pictogramFor
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Entries that still have their video on disk, newest first. Aged-out proofs simply drop off. */
fun proofEntries(entries: List<HistoryEntry>, limit: Int = 12): List<HistoryEntry> =
    entries.asReversed().filter { ProofStore.exists(it.videoPath) }.take(limit)

/** "Video proof" list for History: tap Play for the player sheet. */
@Composable
fun ProofsCard(proofs: List<HistoryEntry>, onPlay: (HistoryEntry) -> Unit) {
    val colors = QiapTheme.colors
    val type = QiapTheme.type
    val context = LocalContext.current
    QiapCard(size = CardSize.Small, verticalArrangement = Arrangement.spacedBy(QiapSpacing.sm)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            QiapText("Video proof", style = type.title)
            QiapText("Kept ${ProofStore.retentionDays(context)} days", style = type.caption, color = colors.ink3)
        }
        if (proofs.isEmpty()) {
            QiapText(
                "Turn on Video proof in an alarm and your workouts show up here. They never leave this phone.",
                style = type.bodySmall,
                color = colors.ink2,
            )
        }
        proofs.forEach { e ->
            Row(horizontalArrangement = Arrangement.spacedBy(QiapSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Pictogram(pictogramFor(e.exerciseId), Modifier.size(32.dp), showFloor = false)
                Column(Modifier.weight(1f)) {
                    QiapText(ExerciseCatalog.byId(e.exerciseId)?.name ?: e.exerciseId, style = type.label)
                    QiapText(
                        LocalDate.ofEpochDay(e.epochDay).format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())) +
                            " · ${e.reps} ${ExerciseCatalog.byId(e.exerciseId)?.unit ?: "reps"}",
                        style = type.caption,
                        color = colors.ink3,
                    )
                }
                Chip("Play", leadingIcon = QiapIcons.Play, onClick = { onPlay(e) })
            }
        }
    }
}

/** The player: system video view with a scrubber, plus Share through the app's private FileProvider. */
@Composable
fun ProofPlayer(entry: HistoryEntry, onClose: () -> Unit) {
    val colors = QiapTheme.colors
    val context = LocalContext.current
    val path = entry.videoPath ?: return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            QiapText(ExerciseCatalog.byId(entry.exerciseId)?.name ?: entry.exerciseId, style = QiapTheme.type.h3)
            QiapText(
                LocalDate.ofEpochDay(entry.epochDay).format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.getDefault())),
                style = QiapTheme.type.label,
                color = colors.ink2,
            )
        }
        IconTile(QiapIcons.Close, "Close", size = 40.dp, onClick = onClose)
    }
    val player = remember(path) { arrayOfNulls<VideoView>(1) }
    AndroidView(
        modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f).clip(QiapRadius.cardLarge),
        factory = { ctx ->
            VideoView(ctx).also { v ->
                player[0] = v
                v.setMediaController(MediaController(ctx).also { it.setAnchorView(v) })
                v.setVideoPath(path)
                v.setOnPreparedListener { it.isLooping = false; v.start() }
            }
        },
        onRelease = { it.stopPlayback() },
    )
    PillButton(
        "Share",
        style = PillButtonStyle.Secondary,
        leadingIcon = QiapIcons.Share,
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            runCatching {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.proofs", File(path))
                val send = Intent(Intent.ACTION_SEND)
                    .setType("video/mp4")
                    .putExtra(Intent.EXTRA_STREAM, uri)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(Intent.createChooser(send, "Share video proof"))
            }
        },
    )
}
