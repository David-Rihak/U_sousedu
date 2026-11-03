package cz.tp.usousedu

import androidx.compose.ui.window.ComposeUIViewController
import cz.tp.usousedu.DI.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = { initKoin() },
) { App() }
