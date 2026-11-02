package cz.tp.usousedu.navigation

import androidx.navigation.NavHostController

interface INavigationRouter {
    fun toLoginScreen()
    fun toAfterLoginScreen()
    fun navigateBack()
}
