# Technische Auswertung: Single-State-MVVM und reducer-basiertes MVI

**Erhebungsdatum:** 14.11.2026 (Europe/Berlin)
**Projekt:** Terminverwaltung, Android / Jetpack Compose
**Auswertungsstatus:** Detekt- und JaCoCo-Läufe erfolgreich; beide Unit-Testgruppen erfolgreich.

## 1. Ziel und Untersuchungsgegenstand

Untersucht werden die architekturspezifischen Produktionsklassen und Unit-Tests der beiden im Projekt vorhandenen Varianten. Die Auswertung trennt Messwerte von Beobachtungen und Interpretation. Gemeinsame UI-, Datenhaltungs- und Modellklassen bleiben aus den architekturspezifischen Produktionsmessungen ausgeschlossen.

## 2. Verwendete Projektversion und Werkzeuge

| Werkzeug / Konfiguration | Verwendete Version bzw. Zustand |
|---|---|
| Gradle Wrapper | Gradle 9.4.1 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin Gradle Plugin | 2.0.21 |
| Detekt | 1.23.8 |
| JaCoCo Agent und Report | 0.8.13 |
| Java für die Gradle-Läufe | JetBrains Runtime 21.0.10 |
| Detekt-Konfiguration | `app/detekt.yml`, mit `buildUponDefaultConfig = true` |

`CyclomaticComplexMethod` ist in `detekt.yml` aktiviert. Ein eigener Schwellenwert ist dort nicht gesetzt; Detekt 1.23.8 verwendet standardmäßig 15. Die Regel zählt unter anderem Kontrollflussbedingungen, Schleifen, `&&`, `||`, `?:` und `catch`. Die Komplexitätsregel und Detekt-Berichte sind in der [Detekt-1.23.8-Dokumentation](https://detekt.dev/docs/1.23.8/rules/complexity/) beschrieben. Detekt weist separat darauf hin, dass `ComplexityReport` Projektkennzahlen wie SLOC und McCabe-Komplexität erzeugt ([Konfigurations- und Berichtsdokumentation](https://detekt.dev/docs/1.23.8/introduction/configurations/)).

Der Projektordner enthält kein erreichbares Git-Repository: `git rev-parse` schlug mit „not a git repository“ fehl. Deshalb kann kein Commit-Hash für den Bericht angegeben werden.

## 3. Abgrenzung der untersuchten Dateien

Die Dateien existieren im aktuellen Source Set `app/src/main/java` und sind genau die Quelllisten der vorhandenen Detekt- und JaCoCo-Aufgaben.

**MVVM:**

- `AppointmentViewModel.kt`
- `AppointmentUiState.kt`

**MVI:**

- `mvi/MviAppointmentState.kt`
- `mvi/MviAppointmentIntent.kt`
- `mvi/AppointmentReducer.kt`
- `mvi/MviAppointmentViewModel.kt`

**Ausgeschlossen:** `MainActivity.kt`, alle Compose-/Route-/Theme-Komponenten, beide ViewModel-Factories, `Appointment.kt`, Repository, DataSource, DAO, Room-Datenbank sowie generierte Room-Klassen. Unit-Testklassen sind Testeingaben für JaCoCo, aber keine Produktionsklassen des Coverage-Nenners.

Diese Abgrenzung zählt beim MVI die zusätzlichen Architekturbausteine State, Intent und Reducer mit. Die unterschiedlich großen Architekturgrenzen werden bei der Interpretation berücksichtigt.

## 4. Messung der Codekomplexität

Die Werte stammen aus den frisch ausgeführten Tasks `detektMvvmMetrics` und `detektMviMetrics`, jeweils mit Detekt 1.23.8 und derselben Projektkonfiguration. Der MCC-Wert ist die Detekt-Projektsumme über die jeweilige Messgrenze. Der Mittelwert ist daraus abgeleitet: MCC-Summe geteilt durch die im selben Bericht ausgewiesene Funktionszahl. Das Maximum je Einzelfunktion ist in den konfigurierten Standardberichten nicht ausgegeben und bleibt deshalb nicht verfügbar.

| Detekt-Metrik | MVVM | MVI |
|---|---:|---:|
| Kotlin-Dateien | 2 | 4 |
| LOC | 105 | 196 |
| SLOC | 91 | 162 |
| LLOC | 57 | 111 |
| CLOC | 0 | 2 |
| MCC-Summe (Detekt) | 7 | 22 |
| Funktionen (Detekt-Zähler) | 4 | 6 |
| MCC-Mittelwert, abgeleitet | 1,75 | 3,67 |
| Maximale MCC einer Funktion | nicht verfügbar | nicht verfügbar |
| Klassen (Detekt-Zähler) | 2 | 9 |
| Eigenschaften | 4 | 7 |
| Cognitive Complexity (zusätzlicher Detekt-Wert) | 2 | 7 |
| Detekt-Findings | 0 | 0 |

Die Mittelwerte verwenden ausschließlich `MCC-Summe / Detekt-Funktionszahl`; sie sind abgeleitete Kennzahlen, keine direkt ausgegebenen Einzelmethoden-Mittelwerte. Detekt hat keine Finding-Liste mit Einzelmethoden-MCC ausgegeben. Deshalb werden weder ein Maximum noch eine Verteilung pro Funktion behauptet. JaCoCo-Komplexitätszähler werden nicht als Ersatz verwendet, weil sie Bytecode und Compiler-generierte Methoden einschließen und somit eine andere Messdefinition darstellen.

Die Detekt-Berichte dieser Läufe wurden je Architektur separat gesichert:

- `technical_evaluation/generated/detekt/mvvm/detekt.md` und zugehörige HTML-, XML-, SARIF- und Textberichte
- `technical_evaluation/generated/detekt/mvi/detekt.md` und zugehörige HTML-, XML-, SARIF- und Textberichte

Die laufenden Detekt-Tasks verwenden denselben Standardausgabepfad und überschreiben einander. Die Ausgabe des MVVM-Laufs wurde daher direkt nach dessen Ende kopiert, bevor der MVI-Lauf erfolgte.

## 5. Messung der Testbarkeit

### Zählregeln

- **Testmethoden:** Anzahl der `@Test`-Annotationen.
- **TLOC:** physische, nicht leere Codezeilen der gesamten Testdatei; reine Kommentarzeilen mit `//`, `/*` oder `*` sind ausgeschlossen. Setup, Fakes und Hilfsfunktionen zählen mit. Diese projektspezifische Zählregel ist keine Detekt-SLOC-Messung.
- **Assertions:** Anzahl der Aufrufe von JUnit-Assertions, nach Assertion-Methode gezählt. Die Zahl bewertet weder die Präzision der erwarteten Werte noch die fachliche Aussagekraft.

| Testdatei | TLOC | Tests | Assertions und Typen |
|---|---:|---:|---|
| MVVM `AppointmentViewModelTest.kt` | 227 | 8 | 21: `assertEquals` 21 |
| MVI `MviAppointmentViewModelTest.kt` | 199 | 8 | 23: `assertEquals` 11, `assertTrue` 4, `assertFalse` 8 |
| MVI `AppointmentReducerTest.kt` | 146 | 10 | 28: `assertEquals` 8, `assertTrue` 5, `assertFalse` 9, `assertNull` 6 |
| **MVI gesamt** | **345** | **18** | **51: `assertEquals` 19, `assertTrue` 9, `assertFalse` 17, `assertNull` 6** |

### Geprüfte Szenarien und Test-Doubles

**MVVM, 8 ViewModel-Tests:** Laden erfolgreich und fehlgeschlagen; Termin hinzufügen, aktualisieren und löschen; Schreibfehler mit Exception-Nachricht; Update einer unbekannten ID; Fallback-Fehlertext bei Exception ohne Nachricht. Der Test verwendet ein echtes `AppointmentRepository`, aber Fake-DAOs: `FakeAppointmentDao`, `FailingAppointmentDao`, `FailingInsertAppointmentDao` und `FailingInsertWithoutMessageDao`.

**MVI, 8 ViewModel-Tests:** Laden, Hinzufügen, Aktualisieren, Löschen, Ladefehler, Schreibfehler, Update einer unbekannten ID und Fallback-Fehlertext bei Exception ohne Nachricht. Die Tests verwenden einen `FakeAppointmentDataSource` mit steuerbaren Lade- und Schreibfehlern.

**MVI, 10 Reducer-Tests:** Initialzustand; `LoadAppointments`; `AppointmentsLoaded`; `LoadingFailed`; Add-/Update-/Delete-Schreibanforderungen mit `WriteSucceeded`; `WriteFailed` und Fehlerlöschung beim nächsten Schreib-Intent; zusätzlich `WriteSucceeded` und `WriteFailed` ohne vorherige Schreibanforderung. Diese Reducer-Tests verwenden keine Android- oder UI-Komponenten und rufen die reine Reducer-Funktion direkt auf.

Die ViewModel-Tests sind lokale Unit-Tests ohne Compose-UI und echte Room-Datenbank. Sie verwenden `InstantTaskExecutorRule`, `StandardTestDispatcher`, `runTest` und `advanceUntilIdle`. Die MVVM-Tests simulieren den DAO und durchlaufen das Repository; die MVI-ViewModel-Tests simulieren den DataSource-Vertrag. Damit ist die asynchrone Koordination beider ViewModels testbar, benötigt aber je Variante ein Test-Double und Coroutine-Teststeuerung. Der MVI-Reducer bietet zusätzlich einen direkten Testzugang für Zustandsübergänge ohne diese Infrastruktur.

**Interpretation:** MVI hat mehr TLOC, Testmethoden und Assertion-Aufrufe, unter anderem weil die Reducer-Übergänge separat getestet werden. Die Zählwerte allein zeigen keine bessere Testbarkeit. Die assertionreichen Erwartungen sind nicht gleichbedeutend mit unabhängigen fachlichen Anforderungen; Testgranularität und Umfang der gemessenen Architektur unterscheiden sich.

## 6. JaCoCo-Testabdeckung

Die Reports wurden am 14.11.2026 neu erstellt. Jeweils ein erzwungener Testlauf mit gesetzter Architektur-Property lieferte die Rohdaten, und der dazu passende JaCoCo-Task verwendete diese Daten. Der MVVM-Lauf führte 8 MVVM-Tests aus; der MVI-Lauf 18 MVI-Tests. Beide Läufe waren erfolgreich, ohne Fehler oder übersprungene Tests.

| JaCoCo-Zähler | MVVM: abgedeckt / nicht abgedeckt (gesamt) | MVVM-Abdeckung | MVI: abgedeckt / nicht abgedeckt (gesamt) | MVI-Abdeckung |
|---|---:|---:|---:|---:|
| Lines | 59 / 0 (59) | 100,00 % | 104 / 2 (106) | 98,11 % |
| Branches | 5 / 1 (6) | 83,33 % | 37 / 3 (40) | 92,50 % |
| Instructions | 400 / 3 (403) | 99,26 % | 587 / 11 (598) | 98,16 % |
| Methods | 20 / 0 (20) | 100,00 % | 39 / 0 (39) | 100,00 % |
| Classes (Bytecode) | 11 / 0 (11) | 100,00 % | 16 / 0 (16) | 100,00 % |

Line Coverage und Branch Coverage sind unterschiedliche Zähler: MVVM hat alle instrumentierten Quellzeilen mindestens teilweise ausgeführt, aber nicht beide Branch-Ausgänge auf allen Branch-Zeilen. MVI hat 2 nicht abgedeckte Quellzeilen und 3 nicht abgedeckte Branch-Ausgänge. Die Klassen- und Methodenzahlen sind Bytecode-Zahlen und können synthetische Kotlin-Klassen bzw. Lambdas enthalten; sie entsprechen nicht den 2 bzw. 9 Detekt-Quellklassen.

### Nicht vollständig abgedeckte Bereiche

- **MVVM:** `AppointmentViewModel.kt:72` — der Branch in der ID-Prüfung des `firstOrNull`-Prädikats ist nicht vollständig abgedeckt. `AppointmentUiState.kt:5` hat 2 nicht abgedeckte Instruktionen, die Quellzeile selbst zählt JaCoCo dennoch als abgedeckt. Insgesamt gibt es keine nicht abgedeckte Zeile, aber 3 nicht abgedeckte Instruktionen.
- **MVI:** `MviAppointmentViewModel.kt:39` — Branch im ID-Prädikat; `:58` — ein Branch beim Abbruch eines vorherigen Lade-Jobs; `:64` — ein Branch für die Ersatzfehlermeldung bei fehlender Exception-Nachricht. `:79–80` — der `CancellationException`-Pfad wurde nicht ausgeführt und bildet die 2 nicht abgedeckten JaCoCo-Zeilen.

Berichte:

- MVVM HTML: `app/build/reports/jacoco/mvvm/html/index.html`; XML: `app/build/reports/jacoco/mvvm/jacoco.xml`.
- MVI HTML: `app/build/reports/jacoco/mvi/html/index.html`; XML: `app/build/reports/jacoco/mvi/jacoco.xml`.

Beide HTML-Berichte nennen JaCoCo 0.8.13. Die MVVM-Berichte wurden am 14.11.2026 um 13:05:30 Uhr MEZ erstellt, die MVI-Berichte um 13:06:02 Uhr MEZ. Das allgemeine AGP-Berichtverzeichnis `app/build/reports/coverage/test/debug/` stammt vom 17.10.2026 und wurde nicht zur aktuellen Architektur-Auswertung herangezogen.

## 7. Direkter Architekturvergleich

| Vergleichsbereich | MVVM | MVI | Direkt beobachtbarer Unterschied |
|---|---|---|---|
| Architekturdateien | 2 | 4 | MVI trennt State, Intent und Reducer zusätzlich vom ViewModel. |
| Detekt-MCC-Summe | 7 | 22 | MVI hat in seiner breiteren Messgrenze höhere aggregierte MCC. |
| Detekt-Funktionen | 4 | 6 | Unterschiedliche Anzahl ausgewiesener Funktionen. |
| ViewModel-Tests | 8 | 8 | Gleiche Anzahl auf der ViewModel-Ebene und dieselben zentralen CRUD-/Fehlerszenarien. |
| Zusätzliche Reducer-Tests | 0 | 10 | MVI prüft Übergänge und Reducer-Randfälle zusätzlich isoliert. |
| JaCoCo Branches | 6 | 40 | MVI-Messgrenze enthält mehr Verzweigungen im eingeschlossenen Bytecode. |
| JaCoCo Klassen | 11 | 16 | Bytecode-Zähler einschließlich generierter Klassen; nicht Quellklassenzahl. |

Der Vergleich zeigt Unterschiede in den gemessenen Grenzen und in der Verantwortungsverteilung. Er belegt nicht, dass eine Architektur grundsätzlich einfacher, testbarer oder qualitativ besser ist. Die Coverage-Prozentwerte beziehen sich jeweils auf die eingeschlossenen Klassen; die Nenner sind wegen der unterschiedlichen Bestandteile verschieden.

## 8. Methodische Einschränkungen

1. **Unterschiedliche Architekturgrößen:** MVVM umfasst zwei benannte Produktionsdateien, MVI vier. Die MVI-MCC-Summe enthält damit zusätzliche Zustands-, Intent- und Reducer-Logik.
2. **Detekt-MCC-Maximum fehlt:** Der vorhandene Markdown-Bericht gibt MCC-Summe und Funktionsanzahl aus, nicht aber MCC je Einzelfunktion. Die maximale Einzel-MCC ist deshalb nicht verfügbar. Eine zusätzliche Einzelmethoden-Ausgabe oder alternative Erhebung wäre eine Methodenänderung und wurde nicht vorgenommen.
3. **Testumfang ist verschieden:** Beide Varianten haben 8 ViewModel-Tests; MVI hat darüber hinaus 10 Reducer-Tests. Die zusätzliche Zahl misst getrennte Testfälle, nicht automatisch bessere Testbarkeit.
4. **Test-Doubles haben verschiedene Grenzen:** MVVM simuliert den DAO unter dem Repository; MVI simuliert den DataSource-Vertrag. Die reale Room-Implementierung wird von diesen Unit-Tests nicht geprüft.
5. **Coverage erfasst Ausführung, nicht Testqualität:** Hohe Line-, Method- oder Class Coverage garantiert weder vollständige Assertions noch Fehlererkennung. Die fehlenden Branches machen konkrete nicht durchlaufene Pfade sichtbar.
6. **Build-Property:** `appointmentArchitecture` setzt den BuildConfig-Wert; die Produktionsdateien beider Architekturen bleiben im gemeinsamen Debug-Source-Set. Die aktuelle Gradle-Konfiguration filtert jedoch beim explizit mit Property gestarteten `testDebugUnitTest` die passende Testgruppe und JaCoCo filtert die Produktionsklassen pro Report.
7. **Kein Git-Commit verfügbar:** Da der Arbeitsordner kein Git-Repository ist, lassen sich die Messergebnisse nicht über eine Commit-ID identifizieren. Die neu erstellten Dateien und Laufzeitstempel dokumentieren den Stand; für eine zitierfähige Reproduktion sollte zusätzlich ein Commit/Quellstand archiviert werden.
8. **Frühere Ausgaben:** Vorhandene Detekt-, JaCoCo-, Test- und Rohdaten wurden vor den neuen Läufen unter `technical_evaluation/prior_artifacts_before_2026-11-14/` gesichert. Die früheren JaCoCo-Berichte waren vom 21.10.2026 (MVVM/MVI); die alte gemeinsame Coverage-Ausgabe war vom 17.10.2026. Die alte Detekt-Zusammenfassung meldete den 18.10.2026. Diese älteren Dateien wurden nicht als aktuelle Messung verwendet.

## 9. Reproduzierbarkeit und ausgeführte Befehle

Die verwendete Gradle-Version wurde mit `.\gradlew.bat --version` ermittelt. Die aktuellen Messläufe waren:

```
.\gradlew.bat :app:detektMvvmMetrics --rerun-tasks
.\gradlew.bat :app:detektMviMetrics --rerun-tasks
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMvvmReport -PappointmentArchitecture=mvvm --rerun-tasks
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMviReport -PappointmentArchitecture=mvi --rerun-tasks
```

Alle vier Messläufe waren erfolgreich. Die verwendeten Reportdateien und Testresultate sind für jeden Architektur-Lauf im JaCoCo-Bericht und in den je Architektur archivierten Test-XMLs nachvollziehbar.

## 10. Offene Punkte und fehlgeschlagene Messungen

- Keine der oben genannten Messungen ist fehlgeschlagen.
- Die maximale MCC pro Einzelfunktion ist mit den vorhandenen Detekt-Standardberichten nicht verfügbar; sie bleibt im CSV leer.
- Für eine commit-genaue Quellenidentifikation muss der Projektstand außerhalb dieser Auswertung versioniert oder anderweitig archiviert werden.
- Die vollständige Messung der UI-/Room-/Repository-Integration war nicht Teil der angeforderten architekturspezifischen Unit-Test- und Coverage-Grenze.
- Es wurde keine qualitative Testbarkeitsbewertung aus Testanzahl oder Coverage-Prozentwerten abgeleitet.
