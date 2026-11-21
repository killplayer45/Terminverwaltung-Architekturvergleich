package de.paul.asjabin.terminverwaltung

import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val appointmentDao: AppointmentDao) : AppointmentDataSource {
    override val appointments: Flow<List<Appointment>> = appointmentDao.observeAppointments()

    override suspend fun addAppointment(appointment: Appointment) {
        appointmentDao.insert(appointment)
    }

    override suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.update(appointment)
    }

    override suspend fun deleteAppointment(appointmentId: Int) {
        appointmentDao.deleteById(appointmentId)
    }
}
