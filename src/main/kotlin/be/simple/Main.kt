package com.progressbar

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import com.progressbar.ui.OverlayWindow
import com.progressbar.ui.PanelWindow

fun main() = application {
    val app = remember { App.create() }

    LaunchedEffect(Unit) { app.start() }
    DisposableEffect(Unit) {
        onDispose { app.shutdown() }
    }

    PanelWindow(
        app = app as PanelApp,
        user = app.user as PanelUser,
        os = app.os as PanelOS,
        onboardingApp = app as OnboardingApp,
        onboardingUser = app.user as OnboardingUser
    )

    OverlayWindow(
        app = app as OverlayApp,
        user = app.user as OverlayUser,
        os = app.os as OverlayOS
    )
}