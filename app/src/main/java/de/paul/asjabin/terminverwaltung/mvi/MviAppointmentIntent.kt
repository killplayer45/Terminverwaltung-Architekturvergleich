package de.paul.asjabin.terminverwaltung.mvi

import de.paul.asjabin.terminverwaltung.Appointment
import java.time.LocalDateTime

sealed interface MviAppointmentIntent {
    data object LoadAppointments : MviAppointmentIntent
    data class AppointmentsLoaded(val appointments: List<Appointment>) : MviAppointmentIntent
    data class LoadingFailed(val message: String) : MviAppointmentIntent

    data class AddAppointment(
        val title: String,
        val description: String,
        val dateTime: LocalDateTime
    ) : MviAppointmentIntent

    data class UpdateAppointment(
        val appointmentId: Int,
        val title: String,
        val description: String,
        val dateTime: LocalDateTime
    ) : MviAppointmentIntent

    data class DeleteAppointment(val appointmentId: Int) : MviAppointmentIntent

    data object WriteSucceeded : MviAppointmentIntent
    data class WriteFailed(val message: String) : MviAppointmentIntent
}
