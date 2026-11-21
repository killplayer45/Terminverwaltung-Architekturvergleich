package de.paul.asjabin.terminverwaltung.mvi

import de.paul.asjabin.terminverwaltung.Appointment
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppointmentReducerTest {
    @Test
    fun initialStateIsLoadingAndEmpty() {
        val state = MviAppointmentState()

        assertTrue(state.appointments.isEmpty())
        assertTrue(state.isLoading)
        assertFalse(state.isWriting)
        assertNull(state.errorMessage)
    }

    @Test
    fun appointmentsLoadedFinishesLoadingWithAppointments() {
        val appointment = appointment()

        val result = AppointmentReducer.reduce(
            MviAppointmentState(errorMessage = "old error"),
            MviAppointmentIntent.AppointmentsLoaded(listOf(appointment))
        )

        assertEquals(listOf(appointment), result.appointments)
        assertFalse(result.isLoading)
        assertNull(result.errorMessage)
    }

    @Test
    fun loadingFailedEndsLoadingAndSetsError() {
        val result = AppointmentReducer.reduce(
            MviAppointmentState(),
            MviAppointmentIntent.LoadingFailed("Laden fehlgeschlagen")
        )

        assertFalse(result.isLoading)
        assertEquals("Laden fehlgeschlagen", result.errorMessage)
    }

    @Test
    fun loadIntentStartsLoadingAndClearsPreviousError() {
        val result = AppointmentReducer.reduce(
            MviAppointmentState(isLoading = false, errorMessage = "old error"),
            MviAppointmentIntent.LoadAppointments
        )

        assertTrue(result.isLoading)
        assertNull(result.errorMessage)
    }

    @Test
    fun addSuccessCompletesWriteAndLoadedEventAddsAppointment() {
        val appointment = appointment()
        val requested = AppointmentReducer.reduce(
            MviAppointmentState(isLoading = false),
            MviAppointmentIntent.AddAppointment(
                appointment.title,
                appointment.description,
                LocalDateTime.parse(appointment.dateTime)
            )
        )
        val saved = AppointmentReducer.reduce(requested, MviAppointmentIntent.WriteSucceeded)
        val result = AppointmentReducer.reduce(
            saved,
            MviAppointmentIntent.AppointmentsLoaded(listOf(appointment))
        )

        assertFalse(result.isWriting)
        assertEquals(listOf(appointment), result.appointments)
        assertNull(result.errorMessage)
    }

    @Test
    fun updateSuccessKeepsUpdatedAppointmentFromRepository() {
        val existing = appointment()
        val updated = existing.copy(title = "Hausarzt")
        val requested = AppointmentReducer.reduce(
            MviAppointmentState(appointments = listOf(existing), isLoading = false),
            MviAppointmentIntent.UpdateAppointment(
                existing.id,
                updated.title,
                updated.description,
                LocalDateTime.parse(updated.dateTime)
            )
        )
        val result = AppointmentReducer.reduce(
            AppointmentReducer.reduce(requested, MviAppointmentIntent.WriteSucceeded),
            MviAppointmentIntent.AppointmentsLoaded(listOf(updated))
        )

        assertEquals(listOf(updated), result.appointments)
        assertFalse(result.isWriting)
    }

    @Test
    fun deleteSuccessMovesFromPopulatedToEmpty() {
        val existing = appointment()
        val requested = AppointmentReducer.reduce(
            MviAppointmentState(appointments = listOf(existing), isLoading = false),
            MviAppointmentIntent.DeleteAppointment(existing.id)
        )
        val result = AppointmentReducer.reduce(
            AppointmentReducer.reduce(requested, MviAppointmentIntent.WriteSucceeded),
            MviAppointmentIntent.AppointmentsLoaded(emptyList())
        )

        assertTrue(result.appointments.isEmpty())
        assertFalse(result.isWriting)
    }

    @Test
    fun writeFailureSetsErrorAndNextWriteRequestClearsIt() {
        val failed = AppointmentReducer.reduce(
            MviAppointmentState(isLoading = false),
            MviAppointmentIntent.AddAppointment(
                "Zahnarzt",
                "Kontrolle",
                LocalDateTime.parse("2026-10-20T10:00:00")
            )
        ).let { AppointmentReducer.reduce(it, MviAppointmentIntent.WriteFailed("Speichern fehlgeschlagen")) }

        val nextRequest = AppointmentReducer.reduce(
            failed,
            MviAppointmentIntent.DeleteAppointment(1)
        )

        assertFalse(failed.isWriting)
        assertEquals("Speichern fehlgeschlagen", failed.errorMessage)
        assertNull(nextRequest.errorMessage)
        assertTrue(nextRequest.isWriting)
    }

    @Test
    fun writeSucceededWithoutPendingWriteKeepsWriteCountAtZero() {
        val result = AppointmentReducer.reduce(
            MviAppointmentState(isLoading = false),
            MviAppointmentIntent.WriteSucceeded
        )

        assertEquals(0, result.pendingWriteCount)
        assertFalse(result.isWriting)
        assertNull(result.errorMessage)
    }

    @Test
    fun writeFailedWithoutPendingWriteKeepsWriteCountAtZeroAndSetsError() {
        val result = AppointmentReducer.reduce(
            MviAppointmentState(isLoading = false),
            MviAppointmentIntent.WriteFailed("Speichern fehlgeschlagen")
        )

        assertEquals(0, result.pendingWriteCount)
        assertFalse(result.isWriting)
        assertEquals("Speichern fehlgeschlagen", result.errorMessage)
    }

    private fun appointment() = Appointment(
        id = 1,
        title = "Zahnarzt",
        description = "Kontrolltermin",
        dateTime = "2026-10-20T10:00"
    )
}
