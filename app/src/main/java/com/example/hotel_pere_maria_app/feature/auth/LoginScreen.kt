package com.example.hotel_pere_maria_app.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.feature.auth.LoginState
import com.example.hotel_pere_maria_app.feature.auth.LoginViewModel

@Composable
fun Login(
        onLoginSuccess: () -> Unit,
        onNavigateToRegister: () -> Unit = {},
        onNavigateToForgotPassword: () -> Unit = {},
        viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loginStatus) {
        if (uiState.loginStatus is LoginState.Success) {
            onLoginSuccess()
        }
    }

    Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))



        Button(
                onClick = { viewModel.login() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.loginStatus !is LoginState.Loading
        ) {
            if (uiState.loginStatus is LoginState.Loading) {
                CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar Sesión")
            }
        }

        if (uiState.loginStatus is LoginState.Error) {
            val mensaje = (uiState.loginStatus as LoginState.Error).message
            Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Link para recuperar contraseña
        TextButton(onClick = onNavigateToForgotPassword) { Text("¿Olvidaste tu contraseña?") }

        // Link a Registro
        TextButton(onClick = onNavigateToRegister, modifier = Modifier.padding(top = 12.dp)) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

