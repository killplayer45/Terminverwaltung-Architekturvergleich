package de.paul.asjabin.terminverwaltung

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dao: FakeAppointmentDao
    private lateinit var repository: AppointmentRepository
    private lateinit var viewModel: AppointmentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        dao = FakeAppointmentDao()
        repository = AppointmentRepository(dao)
        viewModel = AppointmentViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun addAppointment_savesAppointmentToRepository() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.addAppointment(
            title = "Zahnarzt",
            description = "Kontrolltermin",
            dateTime = LocalDateTime.parse("2026-10-20T10:00:00")
        )

        advanceUntilIdle()

        assertEquals(1, dao.appointments.value.size)
        assertEquals("Zahnarzt", dao.appointments.value.first().title)
        assertEquals(
            "Kontrolltermin",
            dao.appointments.value.first().description
        )
        assertEquals(
            LocalDateTime.parse("2026-10-20T10:00:00").toString(),
            dao.appointments.value.first().dateTime
        )
        assertEquals(dao.appointments.value, viewModel.uiState.value.appointments)
    }

    @Test
    fun deleteAppointment_removesAppointmentFromRepository() = runTest(testDispatcher) {
        // Arrange: Einen Termin vorbereiten
        advanceUntilIdle()

        viewModel.addAppointment(
            title = "Zahnarzt",
            description = "Kontrolltermin",
            dateTime = LocalDateTime.parse("2026-10-20T10:00:00")
        )
        advanceUntilIdle()

        val appointmentId = dao.appointments.value.first().id

        // Act: Termin löschen
        viewModel.deleteAppointment(appointmentId)
        advanceUntilIdle()

        // Assert: Termin darf nicht mehr vorhanden sein
        assertEquals(0, dao.appointments.value.size)
        assertEquals(dao.appointments.value, viewModel.uiState.value.appointments)
    }

    @Test
    fun updateAppointment_updatesExistingAppointment() = runTest(testDispatcher) {
        // Arrange: Termin anlegen
        advanceUntilIdle()

        viewModel.addAppointment(
            title = "Zahnarzt",
            description = "Kontrolltermin",
            dateTime = LocalDateTime.parse("2026-10-20T10:00:00")
        )
        advanceUntilIdle()

        val appointmentId = dao.appointments.value.first().id

        // Act: Bestehenden Termin bearbeiten
        viewModel.updateAppointment(
            appointmentId = appointmentId,
            title = "Hausarzt",
            description = "Blutuntersuchung",
            dateTime = LocalDateTime.parse("2026-10-21T11:30:00")
        )
        advanceUntilIdle()

        // Assert: Änderungen prüfen
        assertEquals(1, dao.appointments.value.size)
        assertEquals("Hausarzt", dao.appointments.value.first().title)
        assertEquals("Blutuntersuchung", dao.appointments.value.first().description)
        assertEquals(
            "2026-10-21T11:30",
            dao.appointments.value.first().dateTime
        )
        assertEquals(dao.appointments.value, viewModel.uiState.value.appointments)
    }

    @Test
    fun loadAppointments_whenRepositorySucceeds_setsAppointmentsAndClearsLoadingAndError() = runTest(testDispatcher) {
        // Arrange: Eine vorhandene Terminliste im beobachteten Repository
        val expectedAppointments = listOf(
            Appointment(
                id = 1,
                title = "Zahnarzt",
                description = "Kontrolltermin",
                dateTime = "2026-10-20T10:00"
            )
        )
        dao.appointments.value = expectedAppointments

        // Act: Den initialen Lade-Flow verarbeiten
        advanceUntilIdle()

        // Assert: Termine wurden veröffentlicht und Laden/Fehler sind abgeschlossen
        assertEquals(expectedAppointments, viewModel.uiState.value.appointments)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun loadAppointments_whenRepositoryFails_setsErrorState() = runTest(testDispatcher) {
        // Arrange: ViewModel mit einem DAO erstellen, das einen Fehler auslöst
        val failingRepository = AppointmentRepository(FailingAppointmentDao())
        val failingViewModel = AppointmentViewModel(failingRepository)

        // Act: Initialisierung und Flow-Verarbeitung ausführen
        advanceUntilIdle()

        // Assert: Ladezustand und Fehlermeldung prüfen
        assertEquals(false, failingViewModel.uiState.value.isLoading)
        assertEquals(
            "Testfehler beim Laden",
            failingViewModel.uiState.value.errorMessage
        )
    }
    private class FakeAppointmentDao : AppointmentDao {

        val appointments = MutableStateFlow<List<Appointment>>(emptyList())
        private var nextId = 1

        override fun observeAppointments(): Flow<List<Appointment>> = appointments

        override suspend fun insert(appointment: Appointment): Long {
            val id = if (appointment.id == 0) nextId++ else appointment.id
            appointments.value = appointments.value + appointment.copy(id = id)
            return id.toLong()
        }

        override suspend fun insertAll(appointments: List<Appointment>) {
            appointments.forEach { insert(it) }
        }

        override suspend fun update(appointment: Appointment) {
            appointments.value = appointments.value.map {
                if (it.id == appointment.id) appointment else it
            }
        }

        override suspend fun deleteById(appointmentId: Int) {
            appointments.value = appointments.value.filterNot {
                it.id == appointmentId
            }
        }
    }

    private class FailingAppointmentDao : AppointmentDao {

        override fun observeAppointments(): Flow<List<Appointment>> {
            return kotlinx.coroutines.flow.flow {
                throw IllegalStateException("Testfehler beim Laden")
            }
        }

        override suspend fun insert(appointment: Appointment): Long = 1L

        override suspend fun insertAll(appointments: List<Appointment>) {}

        override suspend fun update(appointment: Appointment) {}

        override suspend fun deleteById(appointmentId: Int) {}
    }

    @Test
    fun addAppointment_whenRepositoryFails_setsErrorMessage() = runTest(testDispatcher) {
        // Arrange: Repository mit fehlerhaftem DAO
        val failingRepository = AppointmentRepository(FailingInsertAppointmentDao())
        val failingViewModel = AppointmentViewModel(failingRepository)

        advanceUntilIdle()

        // Act: Termin hinzufügen
        failingViewModel.addAppointment(
            title = "Zahnarzt",
            description = "Kontrolltermin",
            dateTime = LocalDateTime.parse("2026-10-20T10:00:00")
        )
        advanceUntilIdle()

        // Assert: Fehlermeldung wird gesetzt
        assertEquals(
            "Testfehler beim Speichern",
            failingViewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun updateAppointment_whenAppointmentDoesNotExist_doesNothing() = runTest(testDispatcher) {
        // Arrange
        advanceUntilIdle()

        // Act: Einen Termin bearbeiten, der nicht existiert
        viewModel.updateAppointment(
            appointmentId = 999,
            title = "Neuer Titel",
            description = "Neue Beschreibung",
            dateTime = LocalDateTime.parse("2026-10-21T11:30:00")
        )
        advanceUntilIdle()

        // Assert: Es wurde kein Termin angelegt
        assertEquals(0, dao.appointments.value.size)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }


    @Test
    fun addAppointment_whenExceptionHasNoMessage_usesFallbackErrorMessage() = runTest(testDispatcher) {
        // Arrange
        val failingRepository = AppointmentRepository(
            FailingInsertWithoutMessageDao()
        )
        val failingViewModel = AppointmentViewModel(failingRepository)

        advanceUntilIdle()

        // Act
        failingViewModel.addAppointment(
            title = "Zahnarzt",
            description = "Kontrolltermin",
            dateTime = LocalDateTime.parse("2026-10-20T10:00:00")
        )
        advanceUntilIdle()

        // Assert
        assertEquals(
            "Der Termin konnte nicht gespeichert werden.",
            failingViewModel.uiState.value.errorMessage
        )
    }

}

private class FailingInsertAppointmentDao : AppointmentDao {

    override fun observeAppointments(): Flow<List<Appointment>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun insert(appointment: Appointment): Long {
        throw IllegalStateException("Testfehler beim Speichern")
    }

    override suspend fun insertAll(appointments: List<Appointment>) {}

    override suspend fun update(appointment: Appointment) {}

    override suspend fun deleteById(appointmentId: Int) {}
}
private class FailingInsertWithoutMessageDao : AppointmentDao {

    override fun observeAppointments(): Flow<List<Appointment>> =
        kotlinx.coroutines.flow.flowOf(emptyList())

    override suspend fun insert(appointment: Appointment): Long {
        throw IllegalStateException()
    }

    override suspend fun insertAll(appointments: List<Appointment>) {}

    override suspend fun update(appointment: Appointment) {}

    override suspend fun deleteById(appointmentId: Int) {}
}