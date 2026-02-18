package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hotel_pere_maria_app.ui.ViewModels.RegisterState
import com.example.hotel_pere_maria_app.ui.ViewModels.RegisterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Register(
        onRegisterSuccess: () -> Unit,
        onNavigateToLogin: () -> Unit,
        viewModel: RegisterViewModel = viewModel()
) {
        val uiState by viewModel.uiState.collectAsState()
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }
        val datePickerState = rememberDatePickerState()

        // Reaccionamos si el estado cambia a Success
        LaunchedEffect(uiState.registerStatus) {
                if (uiState.registerStatus is RegisterState.Success) {
                        onRegisterSuccess()
                }
        }

        // DatePickerDialog
        if (showDatePicker) {
                DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                                TextButton(
                                        onClick = {
                                                datePickerState.selectedDateMillis?.let { millis ->
                                                        val sdf =
                                                                SimpleDateFormat(
                                                                        "yyyy-MM-dd",
                                                                        Locale.getDefault()
                                                                )
                                                        val formatted = sdf.format(Date(millis))
                                                        viewModel.onBirthDateChange(formatted)
                                                }
                                                showDatePicker = false
                                        }
                                ) { Text("Aceptar") }
                        },
                        dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                        Text("Cancelar")
                                }
                        }
                ) { DatePicker(state = datePickerState) }
        }

        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                        text = "Crear Cuenta",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                )
                Text(
                        text = "Rellena tus datos para registrarte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Nombre
                OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Apellidos
                OutlinedTextField(
                        value = uiState.surname,
                        onValueChange = { viewModel.onSurnameChange(it) },
                        label = { Text("Apellidos") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.surnameError != null,
                        supportingText = uiState.surnameError?.let { { Text(it) } },
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Email
                OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = uiState.emailError != null,
                        supportingText = uiState.emailError?.let { { Text(it) } },
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // DNI
                OutlinedTextField(
                        value = uiState.dni,
                        onValueChange = { viewModel.onDniChange(it) },
                        label = { Text("DNI") },
                        placeholder = { Text("12345678X") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.dniError != null,
                        supportingText = uiState.dniError?.let { { Text(it) } },
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Contraseña
                OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation =
                                if (passwordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Text(
                                                text =
                                                        if (passwordVisible) "Ocultar"
                                                        else "Mostrar",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }
                        },
                        isError = uiState.passwordError != null,
                        supportingText = uiState.passwordError?.let { { Text(it) } },
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirmar contraseña
                OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.onConfirmPasswordChange(it) },
                        label = { Text("Confirmar Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation =
                                if (confirmPasswordVisible) VisualTransformation.None
                                else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                                IconButton(
                                        onClick = {
                                                confirmPasswordVisible = !confirmPasswordVisible
                                        }
                                ) {
                                        Text(
                                                text =
                                                        if (confirmPasswordVisible) "Ocultar"
                                                        else "Mostrar",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                        )
                                }
                        },
                        isError = uiState.confirmPasswordError != null,
                        supportingText = uiState.confirmPasswordError?.let { { Text(it) } },
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fecha de nacimiento (clickeable, abre DatePickerDialog)
                OutlinedTextField(
                        value = uiState.birthDate,
                        onValueChange = {},
                        label = { Text("Fecha de Nacimiento") },
                        placeholder = { Text("Pulsa para seleccionar") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = false,
                        isError = uiState.birthDateError != null,
                        supportingText = uiState.birthDateError?.let { { Text(it) } },
                        singleLine = true
                )
                // Botón para abrir el DatePicker (el campo disabled no captura clicks bien)
                TextButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                ) { Text("📅 Seleccionar Fecha de Nacimiento") }

                Spacer(modifier = Modifier.height(8.dp))

                // Ciudad
                OutlinedTextField(
                        value = uiState.city,
                        onValueChange = { viewModel.onCityChange(it) },
                        label = { Text("Ciudad (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sexo (FilterChips)
                Text(
                        text = "Sexo",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                )
                Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        listOf("M" to "Hombre", "F" to "Mujer", "Other" to "Otro").forEach {
                                (value, label) ->
                                FilterChip(
                                        selected = uiState.gender == value,
                                        onClick = { viewModel.onGenderChange(value) },
                                        label = { Text(label) }
                                )
                        }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Registrarse
                Button(
                        onClick = { viewModel.register() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.registerStatus !is RegisterState.Loading
                ) {
                        if (uiState.registerStatus is RegisterState.Loading) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                )
                        } else {
                                Text("Registrarse")
                        }
                }

                // Mensaje de error general
                if (uiState.registerStatus is RegisterState.Error) {
                        val mensaje = (uiState.registerStatus as RegisterState.Error).message
                        Text(
                                text = mensaje,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                        )
                }

                // Link a Login
                TextButton(onClick = onNavigateToLogin, modifier = Modifier.padding(top = 8.dp)) {
                        Text("¿Ya tienes cuenta? Inicia sesión")
                }

                Spacer(modifier = Modifier.height(32.dp))
        }
}
