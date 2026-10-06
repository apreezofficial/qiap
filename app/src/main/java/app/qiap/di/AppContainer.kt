package app.qiap.di

import android.content.Context
import java.time.Clock

/**
 * Manual DI root. One instance per process, owned by [app.qiap.QiapApp].
 * Add dependencies here as plain constructor calls (`val x by lazy { X(...) }`); no framework.
 */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext

    /** Injected everywhere time matters so alarm math is testable. */
    val clock: Clock = Clock.systemDefaultZone()
}
