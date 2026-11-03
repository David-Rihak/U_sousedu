package cz.tp.usousedu.ui.screens.login

import cz.tp.usousedu.data.auth.IGoogleIdentityProvider

interface LoginActions {
    fun login(email: String, password: String)
    fun loginWithGoogle(identityProvider: IGoogleIdentityProvider)
}
