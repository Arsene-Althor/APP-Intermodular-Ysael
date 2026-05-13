package com.example.hotel_pere_maria_app.ui.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Models.BookingSearchState
import com.example.hotel_pere_maria_app.ui.Models.Room
import com.example.hotel_pere_maria_app.ui.Models.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RoomSortOption(val label: String) {
    RATING_DESC("Valoración: mayor primero"),
    RATING_ASC("Valoración: menor primero"),
    PRICE_ASC("Precio: más barato"),
    PRICE_DESC("Precio: más caro"),
    NAME("Tipo A–Z"),
}

class SearchResultsViewModel : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _sortBy = MutableStateFlow(RoomSortOption.RATING_DESC)
    val sortBy: StateFlow<RoomSortOption> = _sortBy.asStateFlow()

    /** Filtros extra (UI); lógica backend en iteración siguiente. */
    val extraAmenityOptions =
        listOf(
            "Desayuno",
            "Spa",
            "Discoteca",
            "Minibar gratis",
            "Parking",
            "Wi-Fi premium",
            "Traslado aeropuerto",
        )

    private val _selectedAmenities = MutableStateFlow<Set<String>>(emptySet())
    val selectedAmenities: StateFlow<Set<String>> = _selectedAmenities.asStateFlow()

    private var rawList: List<Room> = emptyList()

    init {
        runSearch()
    }

    fun runSearch() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val c = BookingSearchState.criteria.value
            if (c == null) {
                _error.value = "No hay criterios de búsqueda. Vuelve a la pantalla principal."
                _rooms.value = emptyList()
                rawList = emptyList()
                _loading.value = false
                return@launch
            }
            RoomRepository.fetchRooms()
            RoomRepository.fetchAvailableRoomsByDates(c.checkIn, c.checkOut)
            val base =
                RoomRepository.availableRooms.value.ifEmpty { RoomRepository.rooms.value }
            rawList =
                base.filter { room ->
                    room.isInService() &&
                        room.max_occupancy >= c.guests &&
                        room.price_per_night >= c.priceMin - 0.01 &&
                        room.price_per_night <= c.priceMax + 0.01
                }
            refreshSorted()
            _loading.value = false
        }
    }

    fun setSort(option: RoomSortOption) {
        _sortBy.value = option
        refreshSorted()
    }

    fun toggleExtraAmenity(key: String) {
        _selectedAmenities.update { cur ->
            if (cur.contains(key)) cur - key else cur + key
        }
    }

    private fun refreshSorted() {
        _rooms.value = applySort(rawList, _sortBy.value)
    }

    private fun applySort(list: List<Room>, sort: RoomSortOption): List<Room> =
        when (sort) {
            RoomSortOption.RATING_DESC -> list.sortedByDescending { it.rate }
            RoomSortOption.RATING_ASC -> list.sortedBy { it.rate }
            RoomSortOption.PRICE_ASC -> list.sortedBy { it.price_per_night }
            RoomSortOption.PRICE_DESC -> list.sortedByDescending { it.price_per_night }
            RoomSortOption.NAME -> list.sortedBy { it.type.lowercase() }
        }
}
