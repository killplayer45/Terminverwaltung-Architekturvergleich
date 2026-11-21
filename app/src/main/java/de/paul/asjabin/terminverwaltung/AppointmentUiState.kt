package de.paul.asjabin.terminverwaltung

data class AppointmentUiState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
