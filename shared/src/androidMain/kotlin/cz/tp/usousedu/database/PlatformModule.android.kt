package cz.tp.usousedu.database

import androidx.room.Room
import cz.tp.usousedu.data.session.SESSION_FILE_NAME
import cz.tp.usousedu.data.session.createSessionDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { createSessionDataStore(androidContext().filesDir.resolve(SESSION_FILE_NAME).absolutePath) }
    single {
        val context = androidContext()
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = context.getDatabasePath(DATABASE_NAME).absolutePath,
        )
    }
}
