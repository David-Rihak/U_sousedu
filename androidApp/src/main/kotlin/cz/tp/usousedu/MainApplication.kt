package cz.tp.usousedu

import android.app.Application
import android.content.pm.ApplicationInfo
import cz.tp.usousedu.DI.AppConfig
import cz.tp.usousedu.DI.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        initKoin(AppConfig(isDebug = isDebuggable)) {
            androidLogger()
            androidContext(this@MainApplication)
        }
    }
}
