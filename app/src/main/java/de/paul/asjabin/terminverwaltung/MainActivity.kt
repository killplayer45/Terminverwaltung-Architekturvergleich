
package de.paul.asjabin.terminverwaltung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.paul.asjabin.terminverwaltung.mvi.MviAppointmentRoute
import de.paul.asjabin.terminverwaltung.mvi.MviAppointmentViewModel
import de.paul.asjabin.terminverwaltung.mvi.MviAppointmentViewModelFactory
import de.paul.asjabin.terminverwaltung.ui.theme.TerminverwaltungTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TerminverwaltungTheme {
                val repository = (application as TerminverwaltungApplication).appointmentRepository
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (BuildConfig.APPOINTMENT_ARCHITECTURE == "mvi") {
                        val mviViewModel: MviAppointmentViewModel = viewModel(
                            factory = MviAppointmentViewModelFactory(repository)
                        )
                        val state by mviViewModel.state.collectAsState()

                        MviAppointmentRoute(
                            state = state,
                            onIntent = mviViewModel::dispatch,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        val appointmentViewModel: AppointmentViewModel = viewModel(
                            factory = AppointmentViewModelFactory(repository)
                        )
                        val uiState by appointmentViewModel.uiState.collectAsState()

                        AppointmentScreen(
                            appointments = uiState.appointments,
                            isLoading = uiState.isLoading,
                            errorMessage = uiState.errorMessage,
                            onAddAppointment = appointmentViewModel::addAppointment,
                            onDeleteAppointment = appointmentViewModel::deleteAppointment,
                            onUpdateAppointment = appointmentViewModel::updateAppointment,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentScreen(
    appointments: List<Appointment>,
    isLoading: Boolean,
    errorMessage: String?,
    onAddAppointment: (String, String, LocalDateTime) -> Unit,
    onDeleteAppointment: (Int) -> Unit,
    onUpdateAppointment: (Int, String, String, LocalDateTime) -> Unit,
    modifier: Modifier = Modifier
)
{
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var appointmentToEdit by remember { mutableStateOf<Appointment?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Terminverwaltung",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Deine Termine auf einen Blick")
        Spacer(modifier = Modifier.height(24.dp))

        errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
        }

        if (isLoading) {
            CircularProgressIndicator()
        } else if (appointments.isEmpty()) {
            Text("Noch keine Termine vorhanden.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(
                    items = appointments,
                    key = { appointment -> appointment.id }
                ) { appointment ->
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = appointment.title,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(appointment.description)

                        Text(
                            text = LocalDateTime.parse(appointment.dateTime).format(
                                DateTimeFormatter.ofPattern("dd.MM.yyyy 'um' HH:mm")
                            )
                        )

                        TextButton(
                            onClick = {
                                onDeleteAppointment(appointment.id)
                            }
                        ) {
                            Text("Termin löschen")
                        }


                        TextButton(
                            onClick = {
                                appointmentToEdit = appointment
                            }
                        ) {
                            Text("Termin bearbeiten")
                        }


                    }
                }

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Termin erstellen")
        }
    }

    if (showDialog) {
        NewAppointmentDialog(
            onDismiss = { showDialog = false },
            onSave = { title, description, dateTime ->
                onAddAppointment(title, description, dateTime)
                showDialog = false
            }
        )
    }


    if (appointmentToEdit != null) {
        EditAppointmentDialog(
            appointment = appointmentToEdit!!,
            onDismiss = { appointmentToEdit = null },
            onSave = { title, description, dateTime ->
                onUpdateAppointment(
                    appointmentToEdit!!.id,
                    title,
                    description,
                    dateTime
                )
                appointmentToEdit = null
            }
        )
    }


}

@Composable
fun NewAppointmentDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, LocalDateTime) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("20.10.2026") }
    var time by remember { mutableStateOf("10:00") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Neuen Termin erstellen") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Datum (TT.MM.JJJJ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Uhrzeit (HH:MM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val cleanTitle = title.trim()
                        if (cleanTitle.isEmpty()) {
                            errorMessage = "Bitte gib einen Titel ein."
                        } else {
                            val parsedDate = java.time.LocalDate.parse(
                                date.trim(),
                                DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            )
                            val parsedTime = java.time.LocalTime.parse(
                                time.trim(),
                                DateTimeFormatter.ofPattern("HH:mm")
                            )

                            onSave(
                                cleanTitle,
                                description.trim(),
                                LocalDateTime.of(parsedDate, parsedTime)
                            )
                        }
                    } catch (_: Exception) {
                        errorMessage = "Bitte prüfe Datum und Uhrzeit."
                    }
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

@Composable
fun EditAppointmentDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onSave: (String, String, LocalDateTime) -> Unit
) {
    var title by remember(appointment.id) {
        mutableStateOf(appointment.title)
    }
    var description by remember(appointment.id) {
        mutableStateOf(appointment.description)
    }
    var date by remember(appointment.id) {
        mutableStateOf(
            LocalDateTime.parse(appointment.dateTime).format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
            )
        )
    }
    var time by remember(appointment.id) {
        mutableStateOf(
            LocalDateTime.parse(appointment.dateTime).format(
                DateTimeFormatter.ofPattern("HH:mm")
            )
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Termin bearbeiten") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Datum (TT.MM.JJJJ)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Uhrzeit (HH:MM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        val cleanTitle = title.trim()

                        if (cleanTitle.isEmpty()) {
                            errorMessage = "Bitte gib einen Titel ein."
                        } else {
                            val parsedDate = java.time.LocalDate.parse(
                                date.trim(),
                                DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            )
                            val parsedTime = java.time.LocalTime.parse(
                                time.trim(),
                                DateTimeFormatter.ofPattern("HH:mm")
                            )

                            onSave(
                                cleanTitle,
                                description.trim(),
                                LocalDateTime.of(parsedDate, parsedTime)
                            )
                        }
                    } catch (_: Exception) {
                        errorMessage = "Bitte prüfe Datum und Uhrzeit."
                    }
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}