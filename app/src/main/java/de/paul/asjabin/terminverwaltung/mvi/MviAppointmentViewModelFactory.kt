package de.paul.asjabin.terminverwaltung.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.paul.asjabin.terminverwaltung.AppointmentDataSource

class MviAppointmentViewModelFactory(
    private val appointmentDataSource: AppointmentDataSource
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MviAppointmentViewModel::class.java)) {
            return MviAppointmentViewModel(appointmentDataSource) as T
        }
        throw IllegalArgumentException("Unbekannter ViewModel-Typ: ${modelClass.name}")
    }
}
