package de.paul.asjabin.terminverwaltung

import java.time.LocalDateTime

val sampleAppointments: List<Appointment> = listOf(
    Appointment(
        id = 1,
        title = "Zahnarzt",
        description = "Kontrolltermin",
        dateTime = LocalDateTime.parse("2026-10-15T10:00:00").toString()
    ),
    Appointment(
        id = 2,
        title = "Teammeeting",
        description = "Besprechung der aktuellen Aufgaben",
        dateTime = LocalDateTime.parse("2026-10-15T10:00:00").toString()
    ),
    Appointment(
        id = 3,
        title = "Sport",
        description = "Training",
        dateTime = LocalDateTime.parse("2026-10-15T10:00:00").toString()
    )
)
