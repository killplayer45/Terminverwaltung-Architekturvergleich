package de.paul.asjabin.terminverwaltung.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.paul.asjabin.terminverwaltung.Appointment
import de.paul.asjabin.terminverwaltung.AppointmentDataSource
import java.time.LocalDateTime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Coordinates UI intents, pure reductions, and asynchronous repository effects. */
class MviAppointmentViewModel(
    private val appointmentDataSource: AppointmentDataSource
) : ViewModel() {
    private val _state = MutableStateFlow(MviAppointmentState())
    val state: StateFlow<MviAppointmentState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        dispatch(MviAppointmentIntent.LoadAppointments)
    }

    fun dispatch(intent: MviAppointmentIntent) {
        _state.update { currentState -> AppointmentReducer.reduce(currentState, intent) }

        when (intent) {
            is MviAppointmentIntent.AddAppointment -> performWrite {
                appointmentDataSource.addAppointment(intent.toAppointment())
            }

            is MviAppointmentIntent.UpdateAppointment -> performWrite {
                val existingAppointment = _state.value.appointments
                    .firstOrNull { it.id == intent.appointmentId }
                    ?: return@performWrite
                appointmentDataSource.updateAppointment(intent.toAppointment(existingAppointment))
            }

            is MviAppointmentIntent.DeleteAppointment -> performWrite {
                appointmentDataSource.deleteAppointment(intent.appointmentId)
            }

            MviAppointmentIntent.LoadAppointments -> observeAppointments()

            is MviAppointmentIntent.AppointmentsLoaded,
            is MviAppointmentIntent.LoadingFailed,
            MviAppointmentIntent.WriteSucceeded,
            is MviAppointmentIntent.WriteFailed -> Unit
        }
    }

    private fun observeAppointments() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            appointmentDataSource.appointments
                .catch { error ->
                    dispatch(
                        MviAppointmentIntent.LoadingFailed(
                            error.message ?: LOAD_ERROR_MESSAGE
                        )
                    )
                }
                .collect { appointments ->
                    dispatch(MviAppointmentIntent.AppointmentsLoaded(appointments))
                }
        }
    }

    private fun performWrite(operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                dispatch(MviAppointmentIntent.WriteSucceeded)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                dispatch(
                    MviAppointmentIntent.WriteFailed(
                        error.message ?: WRITE_ERROR_MESSAGE
                    )
                )
            }
        }
    }

    private fun MviAppointmentIntent.AddAppointment.toAppointment() = Appointment(
        title = title,
        description = description,
        dateTime = dateTime.toString()
    )

    private fun MviAppointmentIntent.UpdateAppointment.toAppointment(
        existing: Appointment
    ) = existing.copy(
        title = title,
        description = description,
        dateTime = dateTime.toString()
    )

    private companion object {
        const val LOAD_ERROR_MESSAGE = "Termine konnten nicht geladen werden."
        const val WRITE_ERROR_MESSAGE = "Der Termin konnte nicht gespeichert werden."
    }
}
