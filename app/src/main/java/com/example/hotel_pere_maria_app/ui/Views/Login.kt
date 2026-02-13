package com.example.hotel_pere_maria_app.ui.Views

import android.content.res.Resources
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.ui.ViewModels.LoginState
import com.example.hotel_pere_maria_app.ui.ViewModels.LoginViewModel

@Composable
fun Login(onLoginSuccess : () -> Unit,
          viewModel: LoginViewModel = viewModel()
){
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loginStatus) {
        if (uiState.loginStatus is LoginState.Success){
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Iniciar Sesión")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { newText ->
                viewModel.onEmailChange(newText)
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { newText ->
                viewModel.onPasswordChange(newText)
            },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),

        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {viewModel.login()},
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.loginStatus !is LoginState.Loading
        ) {
            if(uiState.loginStatus is LoginState.Loading){
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }else{
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
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginPreview(){
    Login({})
}