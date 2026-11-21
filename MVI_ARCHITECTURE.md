# MVI-Variante

Die App enthält zwei getrennte Präsentationsvarianten. MVVM bleibt die Standardeinstellung.

## Variante auswählen

```powershell
./gradlew.bat :app:assembleDebug
./gradlew.bat :app:assembleDebug -PappointmentArchitecture=mvi
```

`appointmentArchitecture` akzeptiert `mvvm` oder `mvi`. Die Einstellung wird beim Build in `BuildConfig.APPOINTMENT_ARCHITECTURE` geschrieben und ersetzt die vorhandene MVVM-Implementierung nicht.

## Zuständigkeiten

- `MviAppointmentState` enthält den unveränderlichen Darstellungszustand.
- `MviAppointmentIntent` beschreibt UI-Aktionen und interne Lade-/Schreibergebnisse.
- `AppointmentReducer` bildet ausschließlich reine Zustandsübergänge ab. Er startet keine Coroutines und greift nicht auf Android, Compose oder Room zu.
- `MviAppointmentViewModel` nimmt Intents entgegen, führt Repository-Operationen asynchron aus und veröffentlicht Reducer-Ergebnisse als `StateFlow`.
- `AppointmentRepository` implementiert den gemeinsamen `AppointmentDataSource` und bleibt die einzige Persistenzschnittstelle zu Room.
- Beide Varianten verwenden dieselbe `AppointmentScreen`-Oberfläche und dieselben Dialoge. Nur der Weg zwischen UI-Aktionen, ViewModel und Zustand unterscheidet sich.

Die MVI-Klassen liegen unter `app/src/main/java/de/paul/asjabin/terminverwaltung/mvi/`. Der Gradle-Task `:app:detektMviMetrics` erfasst Reducer, State, Intents, MVI-ViewModel und den MVI-UI-Adapter separat; `:app:detektMvvmMetrics` bleibt unverändert. Die jeweilige ViewModel-Factory und der gemeinsam genutzte Screen sind nicht Teil der getrennten Detekt-Messungen.
