package com.example.hotel_pere_maria_app.ui.Views

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hotel_pere_maria_app.ui.Service.MediaUrls
import com.example.hotel_pere_maria_app.ui.ViewModels.ProfileState
import com.example.hotel_pere_maria_app.ui.ViewModels.ProfileViewModel

@Composable
fun Profile(onLogout: () -> Unit, onOpenMyReviews: () -> Unit = {}, viewModel: ProfileViewModel = viewModel()) {
        val uiState by viewModel.uiState.collectAsState()
        val context = LocalContext.current

        // Lanzador para seleccionar imagen de la galería
        val imagePickerLauncher =
                rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                ) { uri -> uri?.let { viewModel.uploadProfileImage(it, context) } }

        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
                // ===== TARJETA DE PERFIL =====
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                // Avatar con foto de perfil
                                Box(
                                        modifier =
                                                Modifier.size(64.dp)
                                                        .then(
                                                                if (uiState.isEditing) {
                                                                        Modifier.clickable {
                                                                                imagePickerLauncher
                                                                                        .launch(
                                                                                                "image/*"
                                                                                        )
                                                                        }
                                                                } else Modifier
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        val imageUrl = MediaUrls.profileImage(uiState.profileImage)
                                        if (imageUrl != null) {
                                                AsyncImage(
                                                        model = imageUrl,
                                                        contentDescription = "Foto de perfil",
                                                        modifier =
                                                                Modifier.size(64.dp)
                                                                        .clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                )
                                        } else {
                                                Card(
                                                        modifier = Modifier.size(64.dp),
                                                        colors =
                                                                CardDefaults.cardColors(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .primary
                                                                )
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Person,
                                                                contentDescription = "Avatar",
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(12.dp),
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimary
                                                        )
                                                }
                                        }
                                        // Indicador de edición de foto
                                        if (uiState.isEditing) {
                                                Card(
                                                        modifier =
                                                                Modifier.size(24.dp)
                                                                        .align(Alignment.BottomEnd),
                                                        colors =
                                                                CardDefaults.cardColors(
                                                                        containerColor =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .tertiary
                                                                )
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "Cambiar foto",
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(4.dp),
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .onTertiary
                                                        )
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = "${uiState.name} ${uiState.surname}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                                text = uiState.email,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color =
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                                .copy(alpha = 0.7f)
                                        )
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                                Text(
                                                        text =
                                                                uiState.role.replaceFirstChar {
                                                                        it.uppercase()
                                                                },
                                                        style =
                                                                MaterialTheme.typography
                                                                        .labelMedium,
                                                        color =
                                                                MaterialTheme.colorScheme
                                                                        .onPrimaryContainer
                                                )
                                                if (uiState.isVIP) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = "VIP",
                                                                modifier = Modifier.size(16.dp),
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .tertiary
                                                        )
                                                        Text(
                                                                text = "VIP",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelSmall,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .tertiary,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                start = 2.dp
                                                                        )
                                                        )
                                                }
                                                if (uiState.discount > 0) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text =
                                                                        "Dto: ${uiState.discount.toInt()}%",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelSmall,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .onPrimaryContainer
                                                        )
                                                }
                                        }
                                }
                        }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ===== DATOS PERSONALES =====
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                                text = "Datos Personales",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(onClick = { viewModel.toggleEditing() }) {
                                Icon(
                                        imageVector =
                                                if (uiState.isEditing) Icons.Default.Person
                                                else Icons.Default.Edit,
                                        contentDescription =
                                                if (uiState.isEditing) "Cancelar" else "Editar",
                                        modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (uiState.isEditing) "Cancelar" else "Editar")
                        }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.isEditing,
                        singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                        value = uiState.surname,
                        onValueChange = { viewModel.onSurnameChange(it) },
                        label = { Text("Apellidos") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.isEditing,
                        singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.onEmailChange(it) },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.isEditing,
                        singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                        value = uiState.dni,
                        onValueChange = {},
                        label = { Text("DNI") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                        value = uiState.city,
                        onValueChange = { viewModel.onCityChange(it) },
                        label = { Text("Ciudad") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.isEditing,
                        singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                        value = uiState.birthDate,
                        onValueChange = {},
                        label = { Text("Fecha de Nacimiento") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        singleLine = true
                )

                // Sexo
                if (uiState.isEditing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sexo", style = MaterialTheme.typography.bodyMedium)
                        Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                listOf("M" to "Hombre", "F" to "Mujer", "Other" to "Otro")
                                        .forEach { (value, label) ->
                                                FilterChip(
                                                        selected = uiState.gender == value,
                                                        onClick = {
                                                                viewModel.onGenderChange(value)
                                                        },
                                                        label = { Text(label) }
                                                )
                                        }
                        }
                } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                                value =
                                        when (uiState.gender) {
                                                "M" -> "Hombre"
                                                "F" -> "Mujer"
                                                else -> "Otro"
                                        },
                                onValueChange = {},
                                label = { Text("Sexo") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                singleLine = true
                        )
                }

                // Botón guardar
                if (uiState.isEditing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                                onClick = { viewModel.saveChanges() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = uiState.saveStatus !is ProfileState.Loading
                        ) {
                                if (uiState.saveStatus is ProfileState.Loading) {
                                        CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                        )
                                } else {
                                        Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Guardar Cambios")
                                }
                        }
                }

                // Mensajes de estado
                when (val status = uiState.saveStatus) {
                        is ProfileState.Success -> {
                                Text(
                                        text = status.message,
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 8.dp)
                                )
                        }
                        is ProfileState.Error -> {
                                Text(
                                        text = status.message,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 8.dp)
                                )
                        }
                        else -> {}
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = "Más información (soporte)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                        onClick = {
                                val intent =
                                        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=IES+Pere+Maria+Orts+i+Bosch+Benidorm"))
                                                .apply { setPackage("com.google.android.apps.maps") }
                                try {
                                        context.startActivity(intent)
                                } catch (e: Exception) {
                                        context.startActivity(
                                                Intent(
                                                        Intent.ACTION_VIEW,
                                                        Uri.parse("geo:0,0?q=IES+Pere+Maria+Orts+i+Bosch+Benidorm")
                                                )
                                        )
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                ) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Mapa / cómo llegar")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                        onClick = {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:965000000")))
                        },
                        modifier = Modifier.fillMaxWidth(),
                ) {
                        Icon(Icons.Default.Phone, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Llamar al hotel")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                        onClick = {
                                val mail =
                                        Intent(Intent.ACTION_SENDTO).apply {
                                                data =
                                                        Uri.parse(
                                                                "mailto:info@hotelperemaria.com?subject=${Uri.encode("Consulta desde la App Móvil")}"
                                                        )
                                        }
                                try {
                                        context.startActivity(mail)
                                } catch (e: Exception) {
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                ) {
                        Icon(Icons.Default.Email, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Enviar correo")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // ===== AJUSTES =====
                Text(
                        text = "Ajustes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                        onClick = onOpenMyReviews,
                        modifier = Modifier.fillMaxWidth(),
                ) {
                        Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Mis reseñas")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Tema claro/oscuro
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Tema",
                                        tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                        text =
                                                if (uiState.isDarkTheme) "Modo Oscuro"
                                                else "Modo Claro",
                                        style = MaterialTheme.typography.bodyLarge
                                )
                        }
                        Switch(
                                checked = uiState.isDarkTheme,
                                onCheckedChange = { viewModel.toggleTheme() }
                        )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ===== BOTON DESACTIVAR CUENTA =====
                // Permite al usuario desactivar su propia cuenta con una confirmacion previa
                OutlinedButton(
                        onClick = { viewModel.showDeactivateConfirmation() },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                )
                ) {
                        Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desactivar Cuenta")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Boton cerrar sesion
                Button(
                        onClick = {
                                viewModel.logout()
                                onLogout()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                )
                ) {
                        Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión")
                }

                Spacer(modifier = Modifier.height(32.dp))
        }

        // ===== DIALOGO DE CONFIRMACION PARA DESACTIVAR CUENTA =====
        // Se muestra cuando el usuario pulsa "Desactivar Cuenta"
        if (uiState.showDeactivateDialog) {
                AlertDialog(
                        onDismissRequest = { viewModel.dismissDeactivateDialog() },
                        title = { Text("Desactivar Cuenta") },
                        text = {
                                Text(
                                        "\u00bfEst\u00e1s seguro de que quieres desactivar tu cuenta? " +
                                                "No podr\u00e1s iniciar sesi\u00f3n hasta que un administrador la reactive."
                                )
                        },
                        // Boton para confirmar la desactivacion
                        confirmButton = {
                                TextButton(
                                        onClick = { viewModel.deactivateAccount { onLogout() } },
                                        colors =
                                                ButtonDefaults.textButtonColors(
                                                        contentColor =
                                                                MaterialTheme.colorScheme.error
                                                )
                                ) { Text("Desactivar") }
                        },
                        // Boton para cancelar
                        dismissButton = {
                                TextButton(onClick = { viewModel.dismissDeactivateDialog() }) {
                                        Text("Cancelar")
                                }
                        }
                )
        }
}
