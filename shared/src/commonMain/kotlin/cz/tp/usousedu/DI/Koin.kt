package cz.tp.usousedu.DI

import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.navigation.NavigationRouterImpl
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {
    single<INavigationRouter> { NavigationRouterImpl() }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule)
    }

// for iOS
fun initKoin() = initKoin {}
