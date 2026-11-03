package cz.tp.usousedu.ui.screens.register

interface RegisterActions {
    fun register(email: String, password: String, passwordConfirmation: String)
}
