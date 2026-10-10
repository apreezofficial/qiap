package app.qiap.alarm

import android.content.Context
import android.util.Log
import androidx.core.util.AtomicFile
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/** One JSON value in device-protected storage, written atomically. Tiny, direct-boot safe. */
class JsonValueStore<T : Any>(context: Context, fileName: String, private val serializer: KSerializer<T>) {
    private val file = AtomicFile(File(context.createDeviceProtectedStorageContext().filesDir, fileName))

    @Synchronized
    fun get(): T? = try {
        if (!file.baseFile.exists()) null else Json.decodeFromString(serializer, file.readFully().decodeToString())
    } catch (e: Exception) {
        Log.e("QiapStore", "unreadable ${file.baseFile.name}, ignoring", e)
        null
    }

    @Synchronized
    fun set(value: T) {
        val out = file.startWrite()
        try {
            out.write(Json.encodeToString(serializer, value).encodeToByteArray())
            file.finishWrite(out)
        } catch (e: Exception) {
            file.failWrite(out)
            Log.e("QiapStore", "could not write ${file.baseFile.name}", e)
        }
    }

    @Synchronized
    fun clear() {
        file.delete()
    }
}

/** What survives a process death while an alarm rings, so the service can ring again. */
@Serializable
data class PersistedRing(val request: RingRequest, val startedAtMs: Long)

/** A snooze waiting to fire, kept so a reboot during the 5 minutes doesn't lose the alarm. */
@Serializable
data class PendingSnooze(val request: RingRequest, val atMs: Long)
