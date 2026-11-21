# Architekturvergleich: Single-State-MVVM und reducer-basiertes MVI

Dieses Repository enthält die Implementierungen und Auswertungsartefakte zur Bachelorarbeit:

**„Vergleich von Single-State-MVVM und reducer-basiertem MVI in Jetpack Compose anhand von Codekomplexität und Testbarkeit“**

## 1. Zielsetzung

Ziel der Untersuchung ist es, Single-State-MVVM und reducer-basiertes MVI im Kontext von Jetpack Compose hinsichtlich ihrer Codekomplexität und Testbarkeit zu vergleichen.

Die zentrale Forschungsfrage lautet:

> Welche Unterschiede zeigen sich zwischen Single-State-MVVM und reducer-basiertem MVI in Jetpack Compose hinsichtlich Codekomplexität und Testbarkeit?

Dazu wurden zwei funktional vergleichbare Varianten einer Terminverwaltungsanwendung implementiert. Die wesentlichen Unterschiede liegen in der Verwaltung und Verarbeitung des Anwendungszustands.

## 2. Projektstruktur

| Verzeichnis / Datei | Beschreibung |
|---|---|
| `app/` | Android-Anwendung einschließlich Quellcode und Tests |
| `app/src/main/` | Implementierung der Anwendung |
| `app/src/test/` | Unit-Tests |
| `technical_evaluation/` | Technische Auswertung und Reproduktionsanleitung |
| `jacoco_evaluation/` | Gesicherte JaCoCo-Auswertungsartefakte |
| `MVI_ARCHITECTURE.md` | Dokumentation der MVI-Architektur |
| `jacoco_evaluation.md` | Dokumentation zur JaCoCo-Auswertung |

## 3. Technische Voraussetzungen

Für den Build und die Ausführung werden folgende Komponenten verwendet:

- Android Studio
- Java Development Kit (JDK) 21
- Gradle Wrapper 9.4.1
- Android Gradle Plugin 8.13.2
- Kotlin 2.0.21
- Detekt 1.23.8
- JaCoCo 0.8.13

Der Gradle Wrapper ist im Repository enthalten. Eine separate Gradle-Installation ist daher nicht erforderlich.

## 4. Anwendung starten

Die Architektur wird über die Gradle-Property `appointmentArchitecture` ausgewählt.

Ohne Angabe der Property wird standardmäßig die MVVM-Implementierung verwendet.

### Single-State-MVVM

```powershell
.\gradlew.bat installDebug -PappointmentArchitecture=mvvm
```

### Reducer-basiertes MVI

```powershell
.\gradlew.bat installDebug -PappointmentArchitecture=mvi
```

Beide Varianten verwenden dieselbe Anwendung und denselben funktionalen Anwendungsumfang. Die Auswahl erfolgt über die Build-Konfiguration.

## 5. Tests ausführen

Die Tests werden abhängig von der gewählten Architektur ausgeführt.

### MVVM

```powershell
.\gradlew.bat :app:testDebugUnitTest -PappointmentArchitecture=mvvm
```

### MVI

```powershell
.\gradlew.bat :app:testDebugUnitTest -PappointmentArchitecture=mvi
```

Die MVVM-Variante enthält Tests für das AppointmentViewModel.

Die MVI-Variante enthält Tests für das MviAppointmentViewModel und den AppointmentReducer.

Insbesondere die Reducer-Tests prüfen die Zustandsübergänge unabhängig von der Benutzeroberfläche und der Datenquelle.

## 6. Technische Metriken reproduzieren

Für die Untersuchung wurden Detekt und JaCoCo verwendet.

### Detekt

MVVM:

```powershell
.\gradlew.bat :app:detektMvvmMetrics --rerun-tasks
```

MVI:

```powershell
.\gradlew.bat :app:detektMviMetrics --rerun-tasks
```

### JaCoCo

MVVM:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMvvmReport -PappointmentArchitecture=mvvm --rerun-tasks
```

MVI:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMviReport -PappointmentArchitecture=mvi --rerun-tasks
```

Die vollständigen Reproduktionsschritte einschließlich der Sicherung der generierten Berichte sind in `technical_evaluation/reproduction_steps.md` dokumentiert.

**Wichtig:** Bei erneuten Messungen können bestehende Build-Ausgaben überschrieben werden. Vorherige Messartefakte müssen daher gesichert werden.

## 7. Untersuchungskriterien

Die technische Untersuchung berücksichtigt unter anderem folgende Kennzahlen:

**Codekomplexität**
- Lines of Code (LOC)
- Source Lines of Code (SLOC)
- Cyclomatic Complexity (MCC)
- Cognitive Complexity

**Testbarkeit**
- Anzahl der Testmethoden
- Test Lines of Code (TLOC)
- Anzahl der Assertions
- Line Coverage
- Branch Coverage
- Instruction Coverage

Die Kennzahlen werden im Kontext der jeweiligen Implementierung interpretiert. Insbesondere Testanzahl und Codeabdeckung sind für sich genommen kein ausreichender Nachweis einer höheren Testbarkeit.

## 8. Ergebnisse und Auswertungsartefakte

Die technischen Kennzahlen sind in `technical_evaluation/technical_metrics.csv` zusammengefasst.

Weitere Auswertungen und Berichte befinden sich in:

- `technical_evaluation/technical_evaluation_report.md`
- `technical_evaluation/generated/`
- `jacoco_evaluation/`

Die Auswertungen beziehen sich auf die dokumentierte Erhebung vom 14.11.2026. Historische Messungen und ergänzende Artefakte sind entsprechend gekennzeichnet.

## 9. Grenzen der Untersuchung

Die Ergebnisse beziehen sich auf die konkret implementierte Terminverwaltungsanwendung und die festgelegten Messgrenzen.

Sie erlauben keine uneingeschränkte Verallgemeinerung auf sämtliche MVVM- und MVI-Implementierungen.

Insbesondere gilt:

- Codeumfang allein bildet die Komplexität nicht vollständig ab.
- Testanzahl und Assertions sind keine unmittelbaren Qualitätsnachweise.
- Code Coverage beschreibt die Ausführung von Code durch Tests, nicht deren vollständige Fehlererkennungsfähigkeit.
- Die Ergebnisse hängen unter anderem von Implementierungsumfang, Testdesign und Messkonfiguration ab.

## 10. Wissenschaftliche Nachvollziehbarkeit

Dieses Repository dient der Dokumentation und Nachvollziehbarkeit der im Rahmen der Bachelorarbeit durchgeführten Implementierung und technischen Untersuchung.

Die Quellcodes, Tests, Messkonfigurationen und Auswertungsartefakte sollen eine unabhängige Prüfung und – unter Berücksichtigung der dokumentierten Voraussetzungen – eine Wiederholung der technischen Messungen ermöglichen.