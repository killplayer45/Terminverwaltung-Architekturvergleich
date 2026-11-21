package de.paul.asjabin.terminverwaltung.mvi

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import de.paul.asjabin.terminverwaltung.Appointment
import de.paul.asjabin.terminverwaltung.AppointmentDataSource
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MviAppointmentViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsAppointmentsIntoMviState() = runTest(testDispatcher) {
        val expected = listOf(appointment())
        val source = FakeAppointmentDataSource(expected)
        val viewModel = MviAppointmentViewModel(source)

        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value.appointments)
        assertFalse(viewModel.state.value.isLoading)
        assertEquals(null, viewModel.state.value.errorMessage)
    }

    @Test
    fun addIntentWritesToRepositoryAndUpdatesState() = runTest(testDispatcher) {
        val source = FakeAppointmentDataSource()
        val viewModel = MviAppointmentViewModel(source)
        advanceUntilIdle()

        viewModel.dispatch(
            MviAppointmentIntent.AddAppointment(
                "Zahnarzt",
                "Kontrolltermin",
                LocalDateTime.parse("2026-10-20T10:00:00")
            )
        )
        advanceUntilIdle()

        assertEquals(1, source.storedAppointments.value.size)
        val storedAppointment = viewModel.state.value.appointments.single()
        assertEquals("Zahnarzt", storedAppointment.title)
        assertEquals("Kontrolltermin", storedAppointment.description)
        assertEquals("2026-10-20T10:00", storedAppointment.dateTime)
        assertFalse(viewModel.state.value.isWriting)
    }

    @Test
    fun updateIntentUpdatesExistingAppointment() = runTest(testDispatcher) {
        val existing = appointment()
        val source = FakeAppointmentDataSource(listOf(existing))
        val viewModel = MviAppointmentViewModel(source)
        advanceUntilIdle()

        viewModel.dispatch(
            MviAppointmentIntent.UpdateAppointment(
                appointmentId = 1,
                title = "Hausarzt",
                description = "Blutbild",
                dateTime = LocalDateTime.parse("2026-10-21T11:30:00")
            )
        )
        advanceUntilIdle()

        assertEquals(
            existing.copy(
                title = "Hausarzt",
                description = "Blutbild",
                dateTime = "2026-10-21T11:30"
            ),
            viewModel.state.value.appointments.single()
        )
        assertFalse(viewModel.state.value.isWriting)
    }

    @Test
    fun deleteIntentRemovesExistingAppointment() = runTest(testDispatcher) {
        val source = FakeAppointmentDataSource(listOf(appointment()))
        val viewModel = MviAppointmentViewModel(source)
        advanceUntilIdle()

        viewModel.dispatch(MviAppointmentIntent.DeleteAppointment(1))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.appointments.isEmpty())
        assertTrue(source.storedAppointments.value.isEmpty())
        assertFalse(viewModel.state.value.isWriting)
    }

    @Test
    fun loadFailureBecomesStateError() = runTest(testDispatcher) {
        val source = FakeAppointmentDataSource(failOnLoad = true)
        val viewModel = MviAppointmentViewModel(source)
        advanceUntilIdle()

        assertEquals("Ladefehler", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun writeFailureBecomesStateErrorAndEndsWriting() = runTest(testDispatcher) {
        val source = FakeAppointmentDataSource(failOnWrite = true)
        val viewModel = MviAppointmentViewModel(source)
        advanceUntilIdle()

        viewModel.dispatch(
            MviAppointmentIntent.AddAppointment(
                "Zahnarzt",
                "Kontrolle",
                LocalDateTime.parse("2026-10-20T10:00:00")
            )
        )
        advanceUntilIdle()

        assertEquals("Schreibfehler", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isWriting)
    }

    @Test
    fun updateIntentForMissingAppointmentDoesNotChangeAppointmentsOrSetError() = runTest(testDispatcher) {
        val source = FakeAppointmentDataSource()
        val viewModel = MviAppointmentViewModel(source)
        advanceUntilIdle()

        viewModel.dispatch(
            MviAppointmentIntent.UpdateAppointment(
                appointmentId = 999,
                title = "Neuer Titel",
                description = "Neue Beschreibung",
                dateTime = LocalDateTime.parse("2026-10-21T11:30:00")
            )
        )
        advanceUntilIdle()

        assertTrue(source.storedAppointments.value.isEmpty())
        assertTrue(viewModel.state.value.appointments.isEmpty())
        assertEquals(null, viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isWriting)
    }

    @Test
    fun writeFailureWithoutMessageUsesFallbackErrorMessage() = runTest(testDispatcher) {
        val source = FakeAppointmentDataSource(failOnWriteWithoutMessage = true)
        val viewModel = MviAppointmentViewModel(source)
        advanceUntilIdle()

        viewModel.dispatch(
            MviAppointmentIntent.AddAppointment(
                "Zahnarzt",
                "Kontrolltermin",
                LocalDateTime.parse("2026-10-20T10:00:00")
            )
        )
        advanceUntilIdle()

        assertEquals(
            "Der Termin konnte nicht gespeichert werden.",
            viewModel.state.value.errorMessage
        )
        assertFalse(viewModel.state.value.isWriting)
    }

    private fun appointment() = Appointment(
        id = 1,
        title = "Zahnarzt",
        description = "Kontrolltermin",
        dateTime = "2026-10-20T10:00"
    )

    private class FakeAppointmentDataSource(
        initialAppointments: List<Appointment> = emptyList(),
        private val failOnLoad: Boolean = false,
        private val failOnWrite: Boolean = false,
        private val failOnWriteWithoutMessage: Boolean = false
    ) : AppointmentDataSource {
        val storedAppointments = MutableStateFlow(initialAppointments)
        override val appointments: Flow<List<Appointment>> =
            if (failOnLoad) flow { throw IllegalStateException("Ladefehler") } else storedAppointments

        private var nextId = (initialAppointments.maxOfOrNull { it.id } ?: 0) + 1

        override suspend fun addAppointment(appointment: Appointment) {
            failIfRequested()
            storedAppointments.value += appointment.copy(id = nextId++)
        }

        override suspend fun updateAppointment(appointment: Appointment) {
            failIfRequested()
            storedAppointments.value = storedAppointments.value.map {
                if (it.id == appointment.id) appointment else it
            }
        }

        override suspend fun deleteAppointment(appointmentId: Int) {
            failIfRequested()
            storedAppointments.value = storedAppointments.value.filterNot { it.id == appointmentId }
        }

        private fun failIfRequested() {
            if (failOnWriteWithoutMessage) throw IllegalStateException()
            if (failOnWrite) throw IllegalStateException("Schreibfehler")
        }
    }
}
