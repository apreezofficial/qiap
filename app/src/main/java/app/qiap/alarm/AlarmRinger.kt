package app.qiap.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Sound + vibration for a ringing alarm. Uses the alarm audio stream (respects the user's alarm
 * volume, plays through Do Not Disturb's "alarms" allowance), ramps from quiet to [peak] over
 * [RAMP_MS], and loops until stopped.
 *
 * If the system alarm sound can't be opened (no alarm tone set, media provider not ready before
 * first unlock), it falls back to a generated tone. An alarm that rings silently is the worst bug
 * this app can have, so every failure path ends in noise.
 */
class AlarmRinger(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var tone: ToneGenerator? = null
    private var peak = 0.8f
    private var level = 0f
    private var ducked = false
    private var rampStartMs = 0L

    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val ramp = object : Runnable {
        override fun run() {
            val p = ((System.currentTimeMillis() - rampStartMs).toFloat() / RAMP_MS).coerceIn(0f, 1f)
            level = MIN_LEVEL + (peak - MIN_LEVEL) * p
            applyVolume()
            if (p < 1f) handler.postDelayed(this, 250)
        }
    }

    private val toneLoop = object : Runnable {
        override fun run() {
            tone?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 900)
            handler.postDelayed(this, 1500)
        }
    }

    fun start(peakVolume: Float) {
        stop()
        peak = peakVolume.coerceIn(0.1f, 1f)
        ducked = false
        rampStartMs = System.currentTimeMillis()
        if (!startPlayer()) startTone()
        startVibration()
        handler.post(ramp)
    }

    /** Workout in progress: keep ringing (so they don't go back to bed) but quietly. */
    fun duck() {
        ducked = true
        applyVolume()
        vibrator()?.cancel()
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        player?.runCatching { stop(); release() }
        player = null
        tone?.release()
        tone = null
        vibrator()?.cancel()
    }

    private fun startPlayer(): Boolean {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: return false
        return try {
            player = MediaPlayer().apply {
                setAudioAttributes(attributes)
                setDataSource(context, uri)
                isLooping = true
                setVolume(MIN_LEVEL, MIN_LEVEL)
                // If playback dies mid-ring, switch to the tone instead of going silent.
                setOnErrorListener { mp, what, extra ->
                    Log.w(TAG, "player error $what/$extra, switching to tone")
                    mp.release()
                    player = null
                    startTone()
                    true
                }
                prepare()
                start()
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "alarm sound unavailable, using tone", e)
            player?.release()
            player = null
            false
        }
    }

    private fun startTone() {
        if (tone != null) return
        tone = runCatching { ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME) }.getOrNull()
        handler.post(toneLoop)
    }

    private fun applyVolume() {
        val v = if (ducked) level * DUCK_FACTOR else level
        player?.setVolume(v, v)
    }

    private fun startVibration() {
        val v = vibrator() ?: return
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 600, 400, 600, 1200), 0)
        if (Build.VERSION.SDK_INT >= 33) {
            v.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(effect, attributes)
        }
    }

    private fun vibrator(): Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    private companion object {
        const val TAG = "QiapRinger"
        const val RAMP_MS = 30_000L
        const val MIN_LEVEL = 0.15f
        const val DUCK_FACTOR = 0.3f
    }
}
