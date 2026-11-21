package de.paul.asjabin.terminverwaltung

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime

class AppointmentViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState(isLoading = true))

    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.appointments
                .catch { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                }
                .collect { appointments ->
                    _uiState.update {
                        it.copy(appointments = appointments, isLoading = false, errorMessage = null)
                    }
                }
        }
    }

    fun addAppointment(
        title: String,
        description: String,
        dateTime: LocalDateTime
    ) {
        val newAppointment = Appointment(
            title = title,
            description = description,
            dateTime = dateTime.toString()
        )

        viewModelScope.launch {
            runRepositoryOperation {
                repository.addAppointment(newAppointment)
            }
        }
    }

    fun deleteAppointment(appointmentId: Int) {
        viewModelScope.launch {
            runRepositoryOperation {
                repository.deleteAppointment(appointmentId)
            }
        }
    }

    fun updateAppointment(
        appointmentId: Int,
        title: String,
        description: String,
        dateTime: LocalDateTime
    ) {
        viewModelScope.launch {
            runRepositoryOperation {
                val existingAppointment = _uiState.value.appointments
                    .firstOrNull { it.id == appointmentId }
                    ?: return@runRepositoryOperation

                repository.updateAppointment(
                    existingAppointment.copy(
                        title = title,
                        description = description,
                        dateTime = dateTime.toString()
                    )
                )
            }
        }
    }

    private suspend fun runRepositoryOperation(operation: suspend () -> Unit) {
        try {
            operation()
            _uiState.update { it.copy(errorMessage = null) }
        } catch (error: Exception) {
            _uiState.update {
                it.copy(errorMessage = error.message ?: "Der Termin konnte nicht gespeichert werden.")
            }
        }
    }
}