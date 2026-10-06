package app.qiap

import android.app.Application
import app.qiap.di.AppContainer

class QiapApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
