import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "be.simple.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Simple True Interpreter"
            packageVersion = "1.1.0"
        }
    }
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.compose.runtime:runtime:1.7.0")
        force("org.jetbrains.compose.runtime:runtime-saveable:1.7.0")
        force("org.jetbrains.compose.ui:ui:1.7.0")
        force("org.jetbrains.compose.ui:ui-text:1.7.0")
        force("org.jetbrains.compose.ui:ui-graphics:1.7.0")
        force("org.jetbrains.compose.ui:ui-unit:1.7.0")
        force("org.jetbrains.compose.ui:ui-geometry:1.7.0")
        force("org.jetbrains.compose.ui:ui-util:1.7.0")
        force("org.jetbrains.compose.foundation:foundation:1.7.0")
        force("org.jetbrains.compose.foundation:foundation-layout:1.7.0")
        force("org.jetbrains.compose.material:material:1.7.0")
        force("org.jetbrains.compose.material:material-ripple:1.7.0")
        force("org.jetbrains.compose.animation:animation:1.7.0")
        force("org.jetbrains.compose.animation:animation-core:1.7.0")
    }
}