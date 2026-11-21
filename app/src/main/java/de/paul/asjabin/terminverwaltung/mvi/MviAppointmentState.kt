package de.paul.asjabin.terminverwaltung.mvi

import de.paul.asjabin.terminverwaltung.Appointment

data class MviAppointmentState(
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = true,
    val pendingWriteCount: Int = 0,
    val errorMessage: String? = null
) {
    val isWriting: Boolean
        get() = pendingWriteCount > 0
}
