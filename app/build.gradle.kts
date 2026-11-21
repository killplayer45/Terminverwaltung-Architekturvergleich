import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
    jacoco
}
detekt {
    config.setFrom(files("$projectDir/detekt.yml"))
    buildUponDefaultConfig = true
}

val appointmentArchitectureProperty = providers.gradleProperty("appointmentArchitecture")
val appointmentArchitecture = appointmentArchitectureProperty
    .orElse("mvvm")
    .get()
    .lowercase()

require(appointmentArchitecture == "mvvm" || appointmentArchitecture == "mvi") {
    "appointmentArchitecture must be either 'mvvm' or 'mvi'."
}

val appointmentJacocoVersion = "0.8.13"

android {
    namespace = "de.paul.asjabin.terminverwaltung"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.paul.asjabin.terminverwaltung"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APPOINTMENT_ARCHITECTURE", "\"$appointmentArchitecture\"")
    }


    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testCoverage {
        jacocoVersion = appointmentJacocoVersion
    }


}

jacoco {
    toolVersion = appointmentJacocoVersion
}

// An explicit property selects an architecture suite; without it, CLI filters
// such as Android Studio's --tests control test selection. JaCoCo reports below
// remain restricted to the corresponding architecture's production classes.
tasks.withType<Test>().configureEach {
    if (name == "testDebugUnitTest" && appointmentArchitectureProperty.isPresent) {
        filter {
            isFailOnNoMatchingTests = true
            when (appointmentArchitecture) {
                "mvvm" -> includeTestsMatching(
                    "de.paul.asjabin.terminverwaltung.AppointmentViewModelTest"
                )
                "mvi" -> {
                    includeTestsMatching(
                        "de.paul.asjabin.terminverwaltung.mvi.MviAppointmentViewModelTest"
                    )
                    includeTestsMatching(
                        "de.paul.asjabin.terminverwaltung.mvi.AppointmentReducerTest"
                    )
                }
            }
        }
    }
}

fun registerArchitectureCoverageReport(
    architecture: String,
    classPatterns: List<String>
) = tasks.register<JacocoReport>("jacoco${architecture.replaceFirstChar { it.uppercase() }}Report") {
    group = "verification"
    description = "Generates the $architecture architecture JaCoCo report."
    dependsOn("testDebugUnitTest")

    doFirst {
        check(appointmentArchitecture == architecture) {
            "Run this report with -PappointmentArchitecture=$architecture so its tests and classes match."
        }
    }

    executionData.setFrom(
        layout.buildDirectory.file(
            "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"
        )
    )
    classDirectories.setFrom(
        files(
            layout.buildDirectory.dir("tmp/kotlin-classes/debug"),
            layout.buildDirectory.dir(
                "intermediates/javac/debug/compileDebugJavaWithJavac/classes"
            )
        ).asFileTree.matching {
            include(classPatterns)
        }
    )
    sourceDirectories.setFrom(files("src/main/java"))

    reports {
        html.required.set(true)
        html.outputLocation.set(
            layout.buildDirectory.dir("reports/jacoco/$architecture/html")
        )
        xml.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file("reports/jacoco/$architecture/jacoco.xml")
        )
        csv.required.set(false)
    }
}

registerArchitectureCoverageReport(
    architecture = "mvvm",
    classPatterns = listOf(
        "de/paul/asjabin/terminverwaltung/AppointmentViewModel.class",
        "de/paul/asjabin/terminverwaltung/AppointmentViewModel\$*.class",
        "de/paul/asjabin/terminverwaltung/AppointmentUiState.class"
    )
)

registerArchitectureCoverageReport(
    architecture = "mvi",
    classPatterns = listOf(
        "de/paul/asjabin/terminverwaltung/mvi/MviAppointmentState.class",
        "de/paul/asjabin/terminverwaltung/mvi/MviAppointmentIntent*.class",
        "de/paul/asjabin/terminverwaltung/mvi/AppointmentReducer.class",
        "de/paul/asjabin/terminverwaltung/mvi/MviAppointmentViewModel.class",
        "de/paul/asjabin/terminverwaltung/mvi/MviAppointmentViewModel\$*.class"
    )
)

dependencies {
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

}
tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektMvvmMetrics") {
    setSource(
        files(
            "src/main/java/de/paul/asjabin/terminverwaltung/AppointmentViewModel.kt",
            "src/main/java/de/paul/asjabin/terminverwaltung/AppointmentUiState.kt"
        )
    )

    config.setFrom(files("$projectDir/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = false
}

tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektMviMetrics") {
    setSource(
        files(
            "src/main/java/de/paul/asjabin/terminverwaltung/mvi/MviAppointmentState.kt",
            "src/main/java/de/paul/asjabin/terminverwaltung/mvi/MviAppointmentIntent.kt",
            "src/main/java/de/paul/asjabin/terminverwaltung/mvi/AppointmentReducer.kt",
            "src/main/java/de/paul/asjabin/terminverwaltung/mvi/MviAppointmentViewModel.kt"
        )
    )

    config.setFrom(files("$projectDir/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = false
}
