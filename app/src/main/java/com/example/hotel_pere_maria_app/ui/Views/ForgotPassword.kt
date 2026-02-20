package com.example.hotel_pere_maria_app.ui.Views

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.ui.ViewModels.ForgotPasswordState
import com.example.hotel_pere_maria_app.ui.ViewModels.ForgotPasswordViewModel

@Composable
fun ForgotPassword(
        onNavigateToLogin: () -> Unit,
        viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
    ) {
        Text(
                text = "Recuperar Contraseña",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Texto explicativo
        Text(
                text = "Introduce tu email y te enviaremos una contraseña temporal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo de email
        OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Boton enviar
        Button(
                onClick = { viewModel.recoverPassword() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.status !is ForgotPasswordState.Loading
        ) {
            if (uiState.status is ForgotPasswordState.Loading) {
                CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                )
            } else {
                Text("Enviar Contraseña Temporal")
            }
        }

        // Mensajes de estado (exito o error)
        when (val status = uiState.status) {
            is ForgotPasswordState.Success -> {
                Text(
                        text = status.message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                )
            }
            is ForgotPasswordState.Error -> {
                Text(
                        text = status.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                )
            }
            else -> {}
        }

        // Link para volver al login
        TextButton(onClick = onNavigateToLogin, modifier = Modifier.padding(top = 12.dp)) {
            Text("Volver al Inicio de Sesión")
        }
    }
}
