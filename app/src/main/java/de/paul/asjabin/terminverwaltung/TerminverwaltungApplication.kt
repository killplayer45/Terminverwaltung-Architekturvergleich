package de.paul.asjabin.terminverwaltung

import android.app.Application

class TerminverwaltungApplication : Application() {
    val appointmentRepository: AppointmentRepository by lazy {
        AppointmentRepository(AppointmentDatabase.getDatabase(this).appointmentDao())
    }
}
