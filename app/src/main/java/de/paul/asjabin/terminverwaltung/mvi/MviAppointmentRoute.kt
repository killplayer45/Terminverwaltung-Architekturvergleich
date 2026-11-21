package de.paul.asjabin.terminverwaltung.mvi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.paul.asjabin.terminverwaltung.AppointmentScreen

/** Adapts MVI state and intents to the shared, architecture-neutral appointment screen. */
@Composable
@Suppress("FunctionNaming")
fun MviAppointmentRoute(
    state: MviAppointmentState,
    onIntent: (MviAppointmentIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    AppointmentScreen(
        appointments = state.appointments,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onAddAppointment = { title, description, dateTime ->
            onIntent(MviAppointmentIntent.AddAppointment(title, description, dateTime))
        },
        onDeleteAppointment = { appointmentId ->
            onIntent(MviAppointmentIntent.DeleteAppointment(appointmentId))
        },
        onUpdateAppointment = { appointmentId, title, description, dateTime ->
            onIntent(
                MviAppointmentIntent.UpdateAppointment(
                    appointmentId,
                    title,
                    description,
                    dateTime
                )
            )
        },
        modifier = modifier
    )
}
