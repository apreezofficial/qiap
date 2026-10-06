package app.qiap

import android.app.Application
import app.qiap.alarm.AlarmService
import app.qiap.di.AppContainer

class QiapApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        AlarmService.ensureChannel(this)
        // Cheap and idempotent: heals any alarm a missed broadcast left unarmed.
        container.alarmScheduler.syncAll()
    }
}