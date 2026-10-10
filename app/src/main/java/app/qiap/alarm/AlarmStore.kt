package app.qiap.alarm

import android.content.Context
import android.util.Log
import androidx.core.util.AtomicFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * A small JSON-backed list, written atomically (temp file + rename, via [AtomicFile]) so a crash
 * or power cut mid-write never leaves a corrupt file.
 *
 * Lives in device-protected storage: readable right after a reboot, before the user unlocks, so
 * alarms can be rescheduled and can ring on a locked phone (direct boot).
 *
 * Reads happen once at construction (the files are tiny). Writes are synchronous and serialized;
 * callers are UI actions and broadcast receivers, a handful of times a day.
 */
open class JsonListStore<T>(context: Context, fileName: String, private val serializer: KSerializer<T>) {
    private val file = AtomicFile(File(context.createDeviceProtectedStorageContext().filesDir, fileName))
    private val listSerializer = ListSerializer(serializer)
    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<T>> = _items

    private fun load(): List<T> = try {
        if (!file.baseFile.exists()) emptyList() else Json.decodeFromString(listSerializer, file.readFully().decodeToString())
    } catch (e: Exception) {
        // A corrupt file must not take the alarm engine down. Keep it aside for debugging.
        Log.e(TAG, "unreadable ${file.baseFile.name}, starting empty", e)
        file.baseFile.renameTo(File(file.baseFile.path + ".corrupt"))
        emptyList()
    }

    @Synchronized
    protected fun update(transform: (List<T>) -> List<T>): List<T> {
        val next = transform(_items.value)
        val out = file.startWrite()
        try {
            out.write(Json.encodeToString(listSerializer, next).encodeToByteArray())
            file.finishWrite(out)
        } catch (e: Exception) {
            file.failWrite(out)
            throw e
        }
        _items.value = next
        return next
    }

    private companion object {
        const val TAG = "QiapStore"
    }
}

class AlarmStore(context: Context) : JsonListStore<Alarm>(context, "alarms.json", Alarm.serializer()) {
    val alarms: StateFlow<List<Alarm>> get() = items

    fun get(id: Int): Alarm? = items.value.firstOrNull { it.id == id }

    /** Inserts (id <= 0 gets a fresh id) or replaces by id. Returns the stored alarm. */
    fun upsert(alarm: Alarm): Alarm {
        var saved = alarm
        update { list ->
            if (alarm.id <= 0) {
                saved = alarm.copy(id = (list.maxOfOrNull { it.id } ?: 0) + 1)
                (list + saved).sortedBy { it.hour * 60 + it.minute }
            } else {
                list.map { if (it.id == alarm.id) alarm else it }.sortedBy { it.hour * 60 + it.minute }
            }
        }
        return saved
    }

    fun setEnabled(id: Int, enabled: Boolean): Alarm? {
        update { list -> list.map { if (it.id == id) it.copy(enabled = enabled) else it } }
        return get(id)
    }

    fun delete(id: Int) {
        update { list -> list.filterNot { it.id == id } }
    }
}

class HistoryStore(context: Context) : JsonListStore<HistoryEntry>(context, "history.json", HistoryEntry.serializer()) {
    val entries: StateFlow<List<HistoryEntry>> get() = items

    fun record(entry: HistoryEntry) {
        // Keep roughly two years; a seal per day is ~40 KB a year.
        update { list -> (list + entry).takeLast(800) }
    }
}
