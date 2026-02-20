package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.Room
import com.example.hotel_pere_maria_app.ui.Models.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el estado de las habitaciones
 * Maneja la lógica de negocio, filtrado y comunicación con el repositorio
 */
class RoomViewModel : ViewModel() {

    // Estado de las habitaciones desde el repositorio
    private val _allRooms = RoomRepository.rooms
    val isLoading: StateFlow<Boolean> = RoomRepository.isLoading
    val error: StateFlow<String?> = RoomRepository.error

    // Filtros
    private val _selectedType = MutableStateFlow("Todos")
    val selectedType: StateFlow<String> = _selectedType

    private val _selectedAvailability = MutableStateFlow<Boolean?>(null)
    val selectedAvailability: StateFlow<Boolean?> = _selectedAvailability

    // Lista filtrada de habitaciones
    private val _filteredRooms = MutableStateFlow<List<Room>>(emptyList())
    val filteredRooms: StateFlow<List<Room>> = _filteredRooms

    // Habitaciones disponibles para fechas específicas (usada en el diálogo de reserva)
    val availableRooms: StateFlow<List<Room>> = RoomRepository.availableRooms
    val availableLoading: StateFlow<Boolean> = RoomRepository.availableLoading
    val availableError: StateFlow<String?> = RoomRepository.availableError

    // Habitación seleccionada para ver detalles
    private val _selectedRoom = MutableStateFlow<Room?>(null)
    val selectedRoom: StateFlow<Room?> = _selectedRoom

    init {
        // Combinar filtros y aplicarlos automáticamente cuando cambien
        viewModelScope.launch {
            combine(
                _allRooms,
                _selectedType,
                _selectedAvailability
            ) { rooms, type, availability ->
                filterRooms(rooms, type, availability)
            }.collect { filtered ->
                _filteredRooms.value = filtered
            }
        }
    }

    /**
     * Carga las habitaciones desde la API
     */
    fun loadRooms() {
        viewModelScope.launch {
            RoomRepository.fetchRooms()
        }
    }

    /**
     * Filtra las habitaciones según los criterios seleccionados
     */
    private fun filterRooms(
        rooms: List<Room>,
        type: String,
        availability: Boolean?
    ): List<Room> {
        var filtered = rooms

        // Filtrar por tipo
        if (type != "Todos") {
            filtered = filtered.filter { it.type.equals(type, ignoreCase = true) }
        }

        // Filtrar por disponibilidad
        availability?.let { isAvailable ->
            filtered = filtered.filter { it.isAvailable == isAvailable }
        }

        return filtered
    }

    /**
     * Cambia el filtro de tipo de habitación
     */
    fun setTypeFilter(type: String) {
        _selectedType.value = type
    }

    /**
     * Cambia el filtro de disponibilidad
     */
    fun setAvailabilityFilter(available: Boolean?) {
        _selectedAvailability.value = available
    }

    /**
     * Resetea todos los filtros
     */
    fun clearFilters() {
        _selectedType.value = "Todos"
        _selectedAvailability.value = null
    }

    /**
     * Carga una habitación específica por ID para ver detalles
     */
    fun loadRoomDetails(roomId: String) {
        viewModelScope.launch {
            val room = RoomRepository.getRoomById(roomId)
            _selectedRoom.value = room
        }
    }

    /**
     * Limpia la habitación seleccionada
     */
    fun clearSelectedRoom() {
        _selectedRoom.value = null
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        RoomRepository.clearError()
        RoomRepository.clearAvailableError()
    }

    /**
     * Carga habitaciones disponibles para un rango de fechas específico
     * @param checkIn Fecha de check-in (formato: dd/MM/yyyy)
     * @param checkOut Fecha de check-out (formato: dd/MM/yyyy)
     */
    fun loadAvailableRoomsByDates(checkIn: String, checkOut: String) {
        viewModelScope.launch {
            RoomRepository.fetchAvailableRoomsByDates(checkIn, checkOut)
        }
    }

    /**
     * Obtiene los tipos de habitación disponibles para los filtros
     */
    fun getRoomTypes(): List<String> {
        return listOf("Todos", "Individual", "Doble", "Suite")
    }
}
