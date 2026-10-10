package app.qiap.core.common

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Crash-safe, local-only logging (details.md Phase 7). If the app dies from an uncaught
 * exception, a short note goes to a file in the app's private storage and the crash then
 * proceeds as normal. Nothing is ever sent anywhere: the user can share the file from Setup.
 */
object CrashLog {
    private const val MAX_BYTES = 64 * 1024

    fun file(context: Context): File = File(File(context.filesDir, "logs").apply { mkdirs() }, "crash.log")

    fun exists(context: Context): Boolean = file(context).let { it.isFile && it.length() > 0 }

    fun clear(context: Context) {
        file(context).delete()
    }

    /** Chains in front of whatever handler is already installed. Cheap enough for Application.onCreate. */
    fun install(context: Context, versionName: String) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val entry = format(System.currentTimeMillis(), Build.MODEL, Build.VERSION.SDK_INT, versionName, thread.name, error)
                val f = file(app)
                f.writeText(trim(if (f.isFile) f.readText() else "", entry))
            }
            previous?.uncaughtException(thread, error)
        }
    }

    /** One readable entry: when, which phone, which build, and the stack trace. */
    fun format(nowMs: Long, model: String, sdk: Int, versionName: String, thread: String, error: Throwable): String {
        val trace = StringWriter().also { error.printStackTrace(PrintWriter(it)) }.toString()
        return "--- crash at $nowMs ---\nQiap $versionName, $model, Android API $sdk, thread $thread\n$trace\n"
    }

    /** Appends [entry] to [existing], dropping the oldest text so the file stays under [MAX_BYTES]. */
    fun trim(existing: String, entry: String, maxChars: Int = MAX_BYTES): String {
        val combined = existing + entry
        return if (combined.length <= maxChars) combined else combined.takeLast(maxChars)
    }
}
