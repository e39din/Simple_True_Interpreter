import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "be.simple"
version = "1.3.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")

    // D.1 — :schedule (cron-utils)
    implementation("com.cronutils:cron-utils:9.2.1")

    // D.2, D.3 — :sound (MP3)
    implementation("com.googlecode.soundlibs:jlayer:1.0.1.4")
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")

    // D.4 — :websocket (Java-WebSocket)
    implementation("org.java-websocket:Java-WebSocket:1.5.7")

    // D.5 — :send:email (javax.mail)
    implementation("com.sun.mail:javax.mail:1.6.2")

    // D.6 — :terminal (PTY)
    implementation("org.jetbrains.pty4j:pty4j:0.12.13")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

compose.desktop {
    application {
        mainClass = "be.simple.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Simple"
            packageVersion = "1.3.0"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}