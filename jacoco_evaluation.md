# JaCoCo-Auswertung: MVVM und reducer-basiertes MVI

**Projekt:** Terminverwaltung (Jetpack Compose)
**Laufdatum:** 16.11.2026, Europe/Berlin
**Ergebnis:** beide Architektur-Testläufe und JaCoCo-Reports erfolgreich
**JaCoCo:** 0.8.13

## 1. Geprüfte Gradle-Konfiguration

Die Gradle-Konfiguration wurde vor dem Lauf geprüft:

- Root-`build.gradle.kts` deklariert Android, Kotlin, Compose, KSP und Detekt über Aliases mit `apply false`; dort ist kein JaCoCo-Plugin konfiguriert.
- `settings.gradle.kts` richtet Plugin- und Dependency-Repositories ein und inkludiert `:app`.
- `gradle/libs.versions.toml` setzt AGP auf 8.13.2 und das Kotlin-Plugin auf 2.0.21; JaCoCo steht nicht im Version Catalog.
- `app/build.gradle.kts` wendet das Gradle-`jacoco`-Plugin an. `appointmentJacocoVersion = "0.8.13"` wird sowohl an `android.testCoverage.jacocoVersion` als auch an `jacoco.toolVersion` übergeben.
- `debug.enableUnitTestCoverage = true` aktiviert Unit-Test-Coverage für Debug.
- `testDebugUnitTest` filtert bei expliziter `-PappointmentArchitecture`-Property die passende Testgruppe. Ohne Property greift dieser Architekturfilter nicht.
- `jacocoMvvmReport` und `jacocoMviReport` lesen die gemeinsame AGP-`.exec`-Datei, hängen vom Unit-Test-Task ab und schreiben in getrennte Reportverzeichnisse. Ein `doFirst`-Check verweigert eine Architektur-Report-Aufgabe mit nicht passender Property.
- Beide Report-Aufgaben verwenden dieselbe JaCoCo-Version, dieselbe Quelle `src/main/java` und dieselben Reporttypen (HTML und XML). Unterschiedlich sind ausschließlich der Architektur-Quellfilter und die durch die Property gewählte Testgruppe.

## 2. Messgrenze

**MVVM-Tests:** `de.paul.asjabin.terminverwaltung.AppointmentViewModelTest`

**MVI-Tests:** `de.paul.asjabin.terminverwaltung.mvi.MviAppointmentViewModelTest` und `de.paul.asjabin.terminverwaltung.mvi.AppointmentReducerTest`

| Architektur | In der Produktions-Coverage enthaltene Quellklassen |
|---|---|
| MVVM | `AppointmentViewModel` einschließlich compiler-generierter innerer Klassen; `AppointmentUiState` |
| MVI | `MviAppointmentState`; `MviAppointmentIntent` samt generierter Unterklassen; `AppointmentReducer`; `MviAppointmentViewModel` einschließlich compiler-generierter innerer Klassen |

Die Report-Class-Patterns in `app/build.gradle.kts` schließen Testklassen und generierte Room-Klassen aus. Ebenfalls ausgeschlossen sind Compose-UI, `MainActivity`, MVI-Route, beide ViewModel-Factories, Repository/DataSource, Terminmodell, DAO und Room-Datenbank. Die Report-Quelllisten bestätigen die eingeschlossenen Dateien:

- MVVM: `AppointmentViewModel.kt`, `AppointmentUiState.kt`
- MVI: `AppointmentReducer.kt`, `MviAppointmentViewModel.kt`, `MviAppointmentIntent.kt`, `MviAppointmentState.kt`

Die Build-Property schaltet nicht zwischen unterschiedlichen Kotlin-Source-Sets um; beide Implementierungen werden im Debug-Build kompiliert. Sie setzt den BuildConfig-Wert und wählt die Testgruppe. Der JaCoCo-Nenner wird separat durch die obigen Bytecode-Class-Patterns begrenzt.

## 3. Testläufe

| Architektur | Ausgeführte Testklassen | Ausgeführt | Erfolgreich | Fehlgeschlagen | Übersprungen | JUnit-Zeitstempel |
|---|---|---:|---:|---:|---:|---|
| MVVM | `AppointmentViewModelTest` | 8 | 8 | 0 | 0 | 16.11.2026 17:58:45.941 UTC (18:58:45.941 MEZ) |
| MVI | `MviAppointmentViewModelTest`, `AppointmentReducerTest` | 18 | 18 | 0 | 0 | 16.11.2026 17:59:22 UTC (18:59:22 MEZ) |

Die MVI-Zahl setzt sich aus 8 ViewModel- und 10 Reducer-Tests zusammen. Die allgemeine `ExampleUnitTest` wurde durch die expliziten Architekturfilter nicht in diese Architektur-Testgruppen aufgenommen.

## 4. Coverage-Ergebnisse

JaCoCo zählt Instruktionen, Zeilen, Branches, Methoden und Klassen getrennt. „Abgedeckt“ ist der ausgeführte Anteil; die Prozentwerte sind `covered / (covered + missed)`.

| Metrik | MVVM abgedeckt / gesamt | MVVM | MVI abgedeckt / gesamt | MVI |
|---|---:|---:|---:|---:|
| Instructions | 400 / 403 | 99,26 % | 587 / 598 | 98,16 % |
| Lines | 59 / 59 | 100,00 % | 104 / 106 | 98,11 % |
| Branches | 5 / 6 | 83,33 % | 37 / 40 | 92,50 % |
| Methods | 20 / 20 | 100,00 % | 39 / 39 | 100,00 % |
| Classes (Bytecode) | 11 / 11 | 100,00 % | 16 / 16 | 100,00 % |
| JaCoCo-Complexity-Counter, zusätzlich | 22 / 23 | 95,65 % | 56 / 59 | 94,92 % |

Der zusätzliche JaCoCo-Complexity-Counter ist ein Bytecode-Coverage-Zähler. Er wird nicht mit einer Quellcode-MCC-Zahl aus Detekt gleichgesetzt.

## 5. Laufzeit, Reportpfade und Rohdaten

| Architektur | Testlauf (lokal) | Report geschrieben (lokal) |
|---|---|---|
| MVVM | 16.11.2026, 18:58:45 MEZ | 16.11.2026, 18:58:47 MEZ |
| MVI (abschließender isolierter Lauf) | 16.11.2026, 18:59:22 MEZ | 16.11.2026, 19:00:49 MEZ |

Die HTML-Seiten nennen JaCoCo `0.8.13.202504020838` (Version 0.8.13).

- MVVM HTML: `app/build/reports/jacoco/mvvm/html/index.html`
- MVVM XML: `app/build/reports/jacoco/mvvm/jacoco.xml`
- MVI HTML: `app/build/reports/jacoco/mvi/html/index.html`
- MVI XML: `app/build/reports/jacoco/mvi/jacoco.xml`
- MVVM Test-XMLs: `jacoco_evaluation/current/mvvm/test-results/testDebugUnitTest/`
- MVI Test-XMLs: `jacoco_evaluation/current/mvi/test-results/testDebugUnitTest/`
- Jeweils separate Rohdatenkopie: `jacoco_evaluation/current/{mvvm,mvi}/raw/testDebugUnitTest.exec`

Die JaCoCo-Aufgaben teilen sich in `app/build` denselben `.exec`-Ausgabepfad. Vor dem MVVM-Lauf wurden vorherige Reports, Test-XMLs und `.exec`-Rohdaten nach `jacoco_evaluation/archive_before_2026-11-16/` kopiert und danach die relevanten Build-Ausgaben geleert. Die MVVM-Rohdaten wurden anschließend separat gesichert. Vor dem abschließenden MVI-Lauf wurden die gemeinsame `.exec`-Datei, Testresultate und der MVI-Reportordner erneut nach einer Sicherung geleert. Der letzte MVI-Bericht basiert daher auf einem frischen `.exec`-Datensatz des MVI-Laufs. Ein vorheriger MVI-Lauf vom selben Tag ist gesondert unter `jacoco_evaluation/mvi_capture_before_isolated_rerun/` archiviert und wird nicht als finales Ergebnis verwendet.

## 6. Ausgeführte Gradle-Befehle

MVVM:

```
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMvvmReport -PappointmentArchitecture=mvvm --rerun-tasks
```

MVI (abschließender, isolierter Lauf; derselbe Befehl wurde zuvor einmal ausgeführt und anschließend nach Bereinigung der Rohdaten wiederholt):

```
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMviReport -PappointmentArchitecture=mvi --rerun-tasks
```

Alle abschließenden Test-/Coverage-Läufe waren erfolgreich. Gradle gab Hinweise auf veraltete Features mit möglicher Inkompatibilität zu Gradle 10 aus; diese Hinweise führten nicht zu einem fehlgeschlagenen Build.

## 7. Vergleichbarkeit und Einschränkungen

Die Reports verwenden dieselbe JaCoCo-Version, dieselben Coverage-Zähler, dieselbe Debug-Variante und dieselbe Quellstandsbasis. Je Architektur wurden nur die zugehörige Testgruppe und der vorher definierte Produktionsklassenfilter verwendet. Gemeinsame UI-, Datenbank- und Repository-Infrastruktur zählt nicht zum Coverage-Nenner.

Die prozentualen Coverage-Werte beziehen sich auf unterschiedliche Produktionsklassenbestände: MVVM misst ViewModel und UI-State, MVI zusätzlich Intent- und Reducer-Bestandteile. Auch die Testgruppen unterscheiden sich, weil MVI zusätzlich zehn isolierte Reducer-Tests enthält. Die Ergebnisse sind deshalb transparent und reproduzierbar je Messgrenze, aber die Prozentzahlen bilden keinen identischen Nenner und keine identische Anzahl an Testfällen. Die Werte allein belegen keine höhere Testqualität oder Testbarkeit.

Ein Git-Commit-Hash ist nicht verfügbar; `git rev-parse` meldet, dass der Projektordner kein Git-Repository ist. Für eine commit-genaue wissenschaftliche Reproduktion sollte der Quellstand zusätzlich versioniert oder anderweitig archiviert werden. Während dieser Aktualisierung wurden keine Produktions-, Test- oder Gradle-Konfigurationsdateien geändert.