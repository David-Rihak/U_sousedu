package cz.tp.usousedu.ui.screens.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(navigation: INavigationRouter) {
    val viewModel = koinViewModel<RegisterViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is RegisterUIState.Success) {
            navigation.toLandingScreen()
        }
    }

    BaseScreen { paddingValues ->
        RegisterScreenContent(
            paddingValues = paddingValues,
            state = state,
            navigation = navigation,
            actions = viewModel,
        )
    }
}

@Composable
fun RegisterScreenContent(
    paddingValues: PaddingValues,
    state: RegisterUIState,
    navigation: INavigationRouter,
    actions: RegisterActions,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordConfirmation by rememberSaveable { mutableStateOf("") }
    val isLoading = state is RegisterUIState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)

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
        AuthTextField(
            value = passwordConfirmation,
            onValueChange = { passwordConfirmation = it },
            label = "Repeat password",
            isPassword = true,
            enabled = !isLoading,
        )

        if (state is RegisterUIState.Error) {
            ErrorText(state.message)
        }

        Button(
            onClick = { actions.register(email, password, passwordConfirmation) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isLoading) "Creating account..." else "Register")
        }

        TextButton(onClick = { navigation.navigateBack() }, enabled = !isLoading) {
            Text("Already have an account? Sign in")
        }
    }
}
