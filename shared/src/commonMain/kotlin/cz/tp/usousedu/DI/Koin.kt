package cz.tp.usousedu.DI

import cz.tp.usousedu.data.auth.FirebaseAuthRepository
import cz.tp.usousedu.data.auth.IAuthRepository
import cz.tp.usousedu.data.cafes.FirestoreCafeRepository
import cz.tp.usousedu.data.cafes.ICafeRepository
import cz.tp.usousedu.data.customers.FirestoreCustomerRepository
import cz.tp.usousedu.data.customers.ICustomerRepository
import cz.tp.usousedu.ui.screens.staff.StaffViewModel
import cz.tp.usousedu.data.menu.FirestoreMenuRepository
import cz.tp.usousedu.data.menu.IMenuRepository
import cz.tp.usousedu.ui.screens.cafes.CafesViewModel
import cz.tp.usousedu.data.loyalty.FirestoreLoyaltyRepository
import cz.tp.usousedu.data.loyalty.FirestoreRewardsRepository
import cz.tp.usousedu.data.loyalty.ILoyaltyRepository
import cz.tp.usousedu.data.loyalty.IRewardsRepository
import cz.tp.usousedu.data.news.FirestoreNewsRepository
import cz.tp.usousedu.data.news.INewsRepository
import cz.tp.usousedu.ui.screens.menu.MenuViewModel
import cz.tp.usousedu.ui.screens.news.NewsViewModel
import cz.tp.usousedu.ui.screens.profile.ProfileViewModel
import cz.tp.usousedu.data.profile.IUserProfileRepository
import cz.tp.usousedu.data.seed.FirestoreSeeder
import cz.tp.usousedu.database.AppDatabase
import cz.tp.usousedu.database.buildAppDatabase
import cz.tp.usousedu.database.platformModule
import cz.tp.usousedu.ui.screens.debug.SeedViewModel
import cz.tp.usousedu.ui.screens.landing.LandingViewModel
import cz.tp.usousedu.ui.screens.login.LoginViewModel
import cz.tp.usousedu.ui.screens.register.RegisterViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** Build-level settings passed in by the platform app. */
data class AppConfig(
    /** Enables debug tools (e.g. Firestore seed). Android sets it from FLAG_DEBUGGABLE, iOS keeps false. */
    val isDebug: Boolean = false,
)

val dataModule = module {
    single { buildAppDatabase(get()) }
    single { get<AppDatabase>().userDao() }
    // Firebase auth. To switch back to the local mock (Room + DataStore), bind
    // LocalAuthRepository(get(), get()) here instead and call its ensureSeeded() at startup.
    single { FirebaseAuthRepository() }
    single<IAuthRepository> { get<FirebaseAuthRepository>() }
    single<IUserProfileRepository> { get<FirebaseAuthRepository>() }
    single { FirestoreSeeder() }
    single<ICafeRepository> { FirestoreCafeRepository() }
    single<IMenuRepository> { FirestoreMenuRepository() }
    single<INewsRepository> { FirestoreNewsRepository() }
    single<ILoyaltyRepository> { FirestoreLoyaltyRepository() }
    single<IRewardsRepository> { FirestoreRewardsRepository() }
    single<ICustomerRepository> { FirestoreCustomerRepository() }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { LandingViewModel(get(), get(), get(), get(), get()) }
    viewModel { SeedViewModel(get(), get()) }
    viewModel { CafesViewModel(get(), get(), get()) }
    viewModel { MenuViewModel(get(), get(), get()) }
    viewModel { NewsViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get()) }
    viewModel { StaffViewModel(get(), get(), get()) }
}

fun initKoin(config: AppConfig = AppConfig(), appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(module { single { config } }, dataModule, platformModule, viewModelModule)
    }

// for iOS
fun initKoin() = initKoin(AppConfig()) {}
