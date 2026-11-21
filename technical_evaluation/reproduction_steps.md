# Reproduktionsschritte

Die Schritte setzen Windows PowerShell, das Android-Studio-JDK und den vorhandenen Gradle Wrapper voraus. Hier wurde Gradle 9.4.1 mit JDK 21.0.10 verwendet.

Vor einem erneuten Lauf zuerst bestehende Ausgaben aus `app/build/reports/detekt/`, `app/build/reports/jacoco/{mvvm,mvi}/`, `app/build/test-results/testDebugUnitTest/` und `app/build/outputs/unit_test_code_coverage/debugUnitTest/` archivieren. Der Lauf mit `--rerun-tasks` ersetzt diese Ausgaben; die historischen Dateien der vorliegenden Erhebung liegen unter `technical_evaluation/prior_artifacts_before_2026-11-14/`.

```
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat --version

# Detekt-Komplexitätsmessung je expliziter Quellliste
.\gradlew.bat :app:detektMvvmMetrics --rerun-tasks
Copy-Item app/build/reports/detekt technical_evaluation/generated/detekt/mvvm -Recurse -Force
.\gradlew.bat :app:detektMviMetrics --rerun-tasks
Copy-Item app/build/reports/detekt technical_evaluation/generated/detekt/mvi -Recurse -Force

# Property-gesteuerte Tests und passende JaCoCo-Messgrenze
.\gradlew.bat :app:testDebugUnitTest :app:jacocoMvvmReport -PappointmentArchitecture=mvvm --rerun-tasks
Copy-Item app/build/test-results/testDebugUnitTest technical_evaluation/generated/mvvm/test-results/testDebugUnitTest -Recurse -Force
Copy-Item app/build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec technical_evaluation/generated/mvvm/coverage/testDebugUnitTest.exec -Force
New-Item -ItemType Directory -Force .\technical_evaluation\generated\jacoco\mvvm | Out-Null
Copy-Item .\app\build\reports\jacoco\mvvm\* .\technical_evaluation\generated\jacoco\mvvm\ -Recurse -Force

.\gradlew.bat :app:testDebugUnitTest :app:jacocoMviReport -PappointmentArchitecture=mvi --rerun-tasks
Copy-Item app/build/test-results/testDebugUnitTest technical_evaluation/generated/mvi/test-results/testDebugUnitTest -Recurse -Force
Copy-Item app/build/outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec technical_evaluation/generated/mvi/coverage/testDebugUnitTest.exec -Force
New-Item -ItemType Directory -Force .\technical_evaluation\generated\jacoco\mvi | Out-Null
Copy-Item .\app\build\reports\jacoco\mvi\* .\technical_evaluation\generated\jacoco\mvi\ -Recurse -Force
```

JaCoCo-Ausgaben: `app/build/reports/jacoco/mvvm/html/index.html`, `app/build/reports/jacoco/mvvm/jacoco.xml`, `app/build/reports/jacoco/mvi/html/index.html` und `app/build/reports/jacoco/mvi/jacoco.xml`. Detekt verwendet einen gemeinsamen Standardausgabepfad; deshalb die unmittelbar nach jedem Detekt-Lauf erfolgende Kopie je Architektur beibehalten.
