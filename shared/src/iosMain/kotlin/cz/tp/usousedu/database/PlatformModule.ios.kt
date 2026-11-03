package cz.tp.usousedu.database

import androidx.room.Room
import cz.tp.usousedu.data.session.SESSION_FILE_NAME
import cz.tp.usousedu.data.session.createSessionDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private fun documentsPath(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory?.path)
}

actual val platformModule: Module = module {
    single { createSessionDataStore(documentsPath() + "/" + SESSION_FILE_NAME) }
    single { Room.databaseBuilder<AppDatabase>(name = documentsPath() + "/" + DATABASE_NAME) }
}
