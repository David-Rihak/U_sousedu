package cz.tp.usousedu.navigation

interface INavigationRouter {
    fun toLoginScreen()
    fun toRegisterScreen()
    fun toLandingScreen()
    fun navigateBack()
}
