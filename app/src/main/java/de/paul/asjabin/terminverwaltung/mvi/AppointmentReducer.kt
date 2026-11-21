package de.paul.asjabin.terminverwaltung.mvi

/** Pure state transitions: no Android, Compose, coroutine, or persistence work belongs here. */
object AppointmentReducer {
    fun reduce(
        state: MviAppointmentState,
        intent: MviAppointmentIntent
    ): MviAppointmentState = when (intent) {
        MviAppointmentIntent.LoadAppointments -> state.copy(
            isLoading = true,
            errorMessage = null
        )

        is MviAppointmentIntent.AppointmentsLoaded -> state.copy(
            appointments = intent.appointments,
            isLoading = false,
            errorMessage = null
        )

        is MviAppointmentIntent.LoadingFailed -> state.copy(
            isLoading = false,
            errorMessage = intent.message
        )

        is MviAppointmentIntent.AddAppointment,
        is MviAppointmentIntent.UpdateAppointment,
        is MviAppointmentIntent.DeleteAppointment -> state.copy(
            pendingWriteCount = state.pendingWriteCount + 1,
            errorMessage = null
        )

        MviAppointmentIntent.WriteSucceeded -> state.copy(
            pendingWriteCount = (state.pendingWriteCount - 1).coerceAtLeast(0),
            errorMessage = null
        )

        is MviAppointmentIntent.WriteFailed -> state.copy(
            pendingWriteCount = (state.pendingWriteCount - 1).coerceAtLeast(0),
            errorMessage = intent.message
        )
    }
}
