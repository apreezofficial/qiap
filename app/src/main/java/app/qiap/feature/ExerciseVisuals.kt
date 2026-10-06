package app.qiap.feature

import app.qiap.core.designsystem.pictogram.PictogramMotion
import app.qiap.core.designsystem.pictogram.Pictograms

/** Pictogram for a catalog exercise id. Falls back to the squat for ids without art yet. */
fun pictogramFor(exerciseId: String): PictogramMotion = when (exerciseId) {
    "squat" -> Pictograms.Squat
    "pushup" -> Pictograms.PushUp
    "lunge" -> Pictograms.Lunge
    "jack" -> Pictograms.JumpingJack
    "plank" -> Pictograms.Plank
    else -> Pictograms.Squat
}
