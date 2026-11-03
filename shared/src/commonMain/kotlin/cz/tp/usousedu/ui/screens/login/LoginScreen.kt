package cz.tp.usousedu.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tp.usousedu.navigation.INavigationRouter
import cz.tp.usousedu.ui.components.AuthTextField
import cz.tp.usousedu.ui.components.BaseScreen
import cz.tp.usousedu.ui.components.ErrorText
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import usousedu.shared.generated.resources.Res
import usousedu.shared.generated.resources.ic_google

@Composable
fun LoginScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<LoginViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is LoginUIState.Success) {
            navigation.toLandingScreen()
        }
    }

    BaseScreen { paddingValues ->
        LoginScreenContent(
            paddingValues = paddingValues,
            state = state,
            navigation = navigation,
            actions = viewModel,
        )
    }
}

@Composable
fun LoginScreenContent(
    paddingValues: PaddingValues,
    state: LoginUIState,
    navigation: INavigationRouter,
    actions: LoginActions,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val isLoading = state is LoginUIState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                enabled = !isLoading,
            )
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true,
                enabled = !isLoading,
            )

            if (state is LoginUIState.Error) {
                ErrorText(state.message)
            }

            Button(
                onClick = { actions.login(email, password) },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isLoading) "Signing in..." else "Sign in")
            }

            TextButton(onClick = { navigation.toRegisterScreen() }, enabled = !isLoading) {
                Text("Don't have an account? Register")
            }
        }

        OutlinedButton(
            onClick = { actions.loginWithGoogle() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text("Sign in with Google")
        }
    }
}
