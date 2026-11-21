# Architekturbezogene JaCoCo-Auswertung

## Ausführen

Führe pro Architektur einen eigenen Gradle-Lauf aus. Die Gradle-Property wählt sowohl die App-Konfiguration als auch die zugehörige Unit-Testgruppe. Der Report-Task bricht ab, wenn die gewählte Property nicht zu seinem Namen passt.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMvvmReport -PappointmentArchitecture=mvvm --rerun-tasks
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMviReport -PappointmentArchitecture=mvi --rerun-tasks
```

## Messgrenze

Beide Reports verwenden JaCoCo 0.8.13 und dieselben JaCoCo-Metriken: Instruktionen, Branches, Komplexität, Zeilen, Methoden und Klassen. Der Testlauf filtert auf die passenden Unit-Testklassen:

- MVVM: `AppointmentViewModelTest` (8 Tests)
- MVI: `MviAppointmentViewModelTest` und `AppointmentReducerTest` (18 Tests)

Der MVVM-Bericht enthält ausschließlich die Bytecode-Klassen zu `AppointmentViewModel` einschließlich seiner Compiler-generierten inneren Klassen sowie `AppointmentUiState`.

Der MVI-Bericht enthält ausschließlich die Bytecode-Klassen zu `MviAppointmentState`, `MviAppointmentIntent` einschließlich seiner Unterklassen, `AppointmentReducer` und `MviAppointmentViewModel` einschließlich seiner Compiler-generierten inneren Klassen.

Ausgeschlossen sind `MainActivity` und alle Compose-/Route-/Theme-Klassen, beide ViewModel-Factories, Repository und DataSource, Terminmodell, DAO, Room-Datenbank und generierte Room-Klassen. Tests werden nicht als Produktionsklassen in den Report aufgenommen. Damit bildet die Messgrenze die architekturspezifischen Produktionsbestandteile der bestehenden Detekt-Aufgaben ab; die zusätzlichen MVI-Bestandteile State, Intent und Reducer bleiben Teil der MVI-Messung.

## Interpretation und Grenzen

Die Property beeinflusste zuvor nur `BuildConfig.APPOINTMENT_ARCHITECTURE`; beide Architekturen und ihre Tests waren weiterhin Teil desselben Source Sets. Die Testfilterung trennt nun die ausgeführten Tests, während die JaCoCo-Klassenfilter die Produktionsklassen pro Architektur trennen. Beide Reports nutzen daher denselben Agenten-Datentyp, aber jeweils einen neu erzeugten `.exec`-Datensatz aus dem Testlauf derselben Property.

Coverage beschreibt, welcher Anteil der eingeschlossenen Bytecode-Strukturen durch die ausgewählten Tests ausgeführt wurde. Sie beweist weder Vollständigkeit der Testfälle noch generell bessere Testbarkeit. Weil die Architekturgrenzen unterschiedlich viele Komponenten enthalten, sind die Coverage-Werte als Ausführung innerhalb der jeweiligen Messgrenze zu lesen und nicht als alleiniger Qualitätsvergleich.
