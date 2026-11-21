package de.paul.asjabin.terminverwaltung

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppointmentPersistenceTest {
    @Test
    fun appointmentSurvivesClosingAndReopeningDatabase() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "appointment-persistence-test.db"
        context.deleteDatabase(databaseName)

        val appointment = Appointment(
            title = "Persistenztest",
            description = "Wird nach dem Öffnen erneut gelesen",
            dateTime = "2026-10-20T10:00"
        )

        val firstDatabase = Room.databaseBuilder(
            context,
            AppointmentDatabase::class.java,
            databaseName
        ).build()
        try {
            firstDatabase.appointmentDao().insert(appointment)
        } finally {
            firstDatabase.close()
        }

        val reopenedDatabase = Room.databaseBuilder(
            context,
            AppointmentDatabase::class.java,
            databaseName
        ).build()
        try {
            val savedAppointments = reopenedDatabase.appointmentDao()
                .observeAppointments()
                .first()

            assertEquals(1, savedAppointments.size)
            assertEquals(appointment.title, savedAppointments.single().title)
            assertEquals(appointment.description, savedAppointments.single().description)
            assertEquals(appointment.dateTime, savedAppointments.single().dateTime)
            assertTrue(savedAppointments.single().id > 0)
        } finally {
            reopenedDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }
}
