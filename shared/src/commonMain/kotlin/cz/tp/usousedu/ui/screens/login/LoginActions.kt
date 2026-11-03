package cz.tp.usousedu.ui.screens.login

interface LoginActions {
    fun login(email: String, password: String)
    fun loginWithGoogle()
}
