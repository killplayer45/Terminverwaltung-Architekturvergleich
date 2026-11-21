package de.paul.asjabin.terminverwaltung

import kotlinx.coroutines.flow.Flow

/** Shared persistence contract used by the MVVM and MVI presentation variants. */
interface AppointmentDataSource {
    val appointments: Flow<List<Appointment>>

    suspend fun addAppointment(appointment: Appointment)

    suspend fun updateAppointment(appointment: Appointment)

    suspend fun deleteAppointment(appointmentId: Int)
}
