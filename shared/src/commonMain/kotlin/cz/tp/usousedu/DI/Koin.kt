package cz.tp.usousedu.DI

import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.auth.LocalAuthRepository
import cz.tp.usousedu.data.profile.IUserProfileRepository
import cz.tp.usousedu.database.AppDatabase
import cz.tp.usousedu.database.buildAppDatabase
import cz.tp.usousedu.database.platformModule
import cz.tp.usousedu.ui.screens.landing.LandingViewModel
import cz.tp.usousedu.ui.screens.login.LoginViewModel
import cz.tp.usousedu.ui.screens.register.RegisterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val dataModule = module {
    single { buildAppDatabase(get()) }
    single { get<AppDatabase>().userDao() }
    single { LocalAuthRepository(get(), get()) }
    single<IAuthRepository> { get<LocalAuthRepository>() }
    single<IUserProfileRepository> { get<LocalAuthRepository>() }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { LandingViewModel(get(), get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(dataModule, platformModule, viewModelModule)
    }.also { koinApp ->
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            koinApp.koin.get<LocalAuthRepository>().ensureSeeded()
        }
    }

// for iOS
fun initKoin() = initKoin {}
