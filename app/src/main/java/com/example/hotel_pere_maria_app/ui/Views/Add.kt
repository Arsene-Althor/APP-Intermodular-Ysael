package com.example.hotel_pere_maria_app.ui.Views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Add(){
    val checkInState = rememberDatePickerState()
    val checkOutState = rememberDatePickerState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()) // Por si la pantalla es pequeña
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(text = "Selección de Estancia", style = MaterialTheme.typography.headlineSmall)

        // DatePicker de Entrada
        Row {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Fecha de Check-in", style = MaterialTheme.typography.labelLarge)
                DatePicker(
                    state = checkInState,
                    showModeToggle = false, // Quita el icono de edición manual para que sea más limpio
                    title = null,           // Quitamos títulos internos para ahorrar espacio
                    headline = null
                )
            }

            HorizontalDivider()

            // DatePicker de Salida
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Fecha de Check-out", style = MaterialTheme.typography.labelLarge)
                DatePicker(
                    state = checkOutState,
                    showModeToggle = false,
                    title = null,
                    headline = null
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AddPreview(){
    Add()
}