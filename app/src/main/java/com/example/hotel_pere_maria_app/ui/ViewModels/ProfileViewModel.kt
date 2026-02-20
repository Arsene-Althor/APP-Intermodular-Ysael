package com.example.hotel_pere_maria_app.ui.ViewModels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hotel_pere_maria_app.ui.Service.RetrofitClient
import com.example.hotel_pere_maria_app.ui.Service.SessionManager
import com.example.hotel_pere_maria_app.ui.Service.ThemeManager
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

// Estado de la pantalla de perfil
sealed class ProfileState {
    object Idle : ProfileState() // Sin actividad
    object Loading : ProfileState() // Guardando cambios
    data class Success(val message: String) : ProfileState() // Cambios guardados
    data class Error(val message: String) : ProfileState() // Error al guardar
}

// Datos de la UI del perfil
data class ProfileUiState(
        val name: String = "",
        val surname: String = "",
        val email: String = "",
        val dni: String = "",
        val birthDate: String = "",
        val city: String = "",
        val gender: String = "Other",
        val role: String = "client",
        val isVIP: Boolean = false,
        val discount: Double = 0.0,
        val profileImage: String? = null,
        val isEditing: Boolean = false, // ¿Está en modo edición?
        val saveStatus: ProfileState = ProfileState.Idle,
        val isDarkTheme: Boolean = false, // ¿Está el tema oscuro activado?
        val showDeactivateDialog: Boolean = false // ¿Se muestra el diálogo de desactivar cuenta?
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Formateador para mostrar fechas en formato dd/MM/yyyy
    private val displayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    init {
        loadUserData()
        // Observar el estado del tema
        viewModelScope.launch {
            ThemeManager.isDarkTheme.collect { isDark ->
                _uiState.update { it.copy(isDarkTheme = isDark) }
            }
        }
    }

    // Convierte una fecha ISO (ej: "2000-01-15T00:00:00.000Z") a formato dd/MM/yyyy
    private fun formatDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormatter.parse(isoDate)
            if (date != null) displayFormatter.format(date) else isoDate
        } catch (e: Exception) {
            // Si falla el parseo ISO, intentar con formato solo fecha
            try {
                val simpleFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = simpleFormatter.parse(isoDate)
                if (date != null) displayFormatter.format(date) else isoDate
            } catch (e2: Exception) {
                isoDate // Si no se puede parsear, devolver el valor original
            }
        }
    }

    // Carga los datos del usuario desde la sesión en memoria
    private fun loadUserData() {
        val user = SessionManager.userInfo ?: return
        _uiState.update {
            it.copy(
                    name = user.name,
                    surname = user.surname,
                    email = user.email,
                    dni = user.dni,
                    birthDate = formatDate(user.birthDate),
                    city = user.city ?: "",
                    gender = user.gender,
                    role = user.role,
                    isVIP = user.isVIP,
                    discount = user.discount,
                    profileImage = user.profileImage
            )
        }
    }

    fun toggleEditing() {
        _uiState.update { it.copy(isEditing = !it.isEditing, saveStatus = ProfileState.Idle) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }
    fun onSurnameChange(value: String) {
        _uiState.update { it.copy(surname = value) }
    }
    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value) }
    }
    fun onCityChange(value: String) {
        _uiState.update { it.copy(city = value) }
    }
    fun onGenderChange(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun toggleTheme() {
        ThemeManager.toggleTheme()
    }

    // Sube la foto de perfil al servidor
    fun uploadProfileImage(uri: Uri, context: Context) {
        val userId = SessionManager.userInfo?.user_id ?: return

        _uiState.update { it.copy(saveStatus = ProfileState.Loading) }

        viewModelScope.launch {
            try {
                // Leer los bytes de la imagen desde el URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: throw Exception("No se pudo leer la imagen")
                inputStream.close()

                // Obtener el tipo MIME real del archivo
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

                // Determinar extensión basada en mimeType para el nombre del archivo
                val extension =
                        when (mimeType) {
                            "image/png" -> "png"
                            "image/webp" -> "webp"
                            "image/gif" -> "gif"
                            else -> "jpg"
                        }

                val part =
                        MultipartBody.Part.createFormData(
                                "profileImage",
                                "profile_${userId}.$extension",
                                requestBody
                        )

                val response = RetrofitClient.userService.uploadProfileImage(userId, part)

                if (response.isSuccessful) {
                    // Obtener la ruta de la imagen del response
                    val updatedUser = response.body()?.user
                    val imagePath = updatedUser?.profileImage

                    // Actualizar estado local
                    _uiState.update {
                        it.copy(
                                profileImage = imagePath,
                                saveStatus = ProfileState.Success("Foto actualizada")
                        )
                    }

                    // Actualizar SessionManager
                    SessionManager.userInfo =
                            SessionManager.userInfo?.copy(profileImage = imagePath)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    _uiState.update { it.copy(saveStatus = ProfileState.Error(errorBody)) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(saveStatus = ProfileState.Error("Error al subir imagen: ${e.message}"))
                }
            }
        }
    }

    fun saveChanges() {
        val state = _uiState.value
        val userId = SessionManager.userInfo?.user_id ?: return

        _uiState.update { it.copy(saveStatus = ProfileState.Loading) }

        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, String?>()
                val currentUser = SessionManager.userInfo!!

                // Solo enviar campos que cambiaron
                if (state.name != currentUser.name) updates["name"] = state.name
                if (state.surname != currentUser.surname) updates["surname"] = state.surname
                if (state.email != currentUser.email) updates["email"] = state.email
                if (state.city != (currentUser.city ?: ""))
                        updates["city"] = state.city.ifBlank { null }
                if (state.gender != currentUser.gender) updates["gender"] = state.gender

                if (updates.isEmpty()) {
                    _uiState.update {
                        it.copy(isEditing = false, saveStatus = ProfileState.Success("Sin cambios"))
                    }
                    return@launch
                }

                val response = RetrofitClient.userService.modifyUser(userId, updates)

                if (response.isSuccessful) {
                    // Actualizar SessionManager con los nuevos datos
                    SessionManager.userInfo =
                            currentUser.copy(
                                    name = state.name,
                                    surname = state.surname,
                                    email = state.email,
                                    city = state.city.ifBlank { null },
                                    gender = state.gender
                            )
                    _uiState.update {
                        it.copy(
                                isEditing = false,
                                saveStatus = ProfileState.Success("Datos actualizados")
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    _uiState.update { it.copy(saveStatus = ProfileState.Error(errorBody)) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(saveStatus = ProfileState.Error("Error de conexión: ${e.message}"))
                }
            }
        }
    }

    fun logout() {
        SessionManager.clear()
    }

    // Muestra el dialogo de confirmacion para desactivar la cuenta
    fun showDeactivateConfirmation() {
        _uiState.update { it.copy(showDeactivateDialog = true) }
    }

    // Cierra el dialogo sin hacer nada
    fun dismissDeactivateDialog() {
        _uiState.update { it.copy(showDeactivateDialog = false) }
    }

    // Llama a la API para desactivar la cuenta y luego cierra sesion
    fun deactivateAccount(onDeactivated: () -> Unit) {
        _uiState.update { it.copy(saveStatus = ProfileState.Loading, showDeactivateDialog = false) }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.userService.deactivateAccount()

                if (response.isSuccessful) {
                    // Limpiamos la sesion y navegamos al login
                    SessionManager.clear()
                    onDeactivated()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    _uiState.update { it.copy(saveStatus = ProfileState.Error(errorBody)) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(saveStatus = ProfileState.Error("Error de conexión: ${e.message}"))
                }
            }
        }
    }
}
