package com.leireomadina.travelplanner.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.leireomadina.travelplanner.data.auth.AuthState

// Picks the screens from the session, like redirectOptions in the web app's nuxt.config.ts.
// Logged-in and logged-out users get separate AppNavigations, each with its own back
// stack, so logging in or out replaces the whole stack: back after logout can't reach
// Home. Screens never navigate on login or logout themselves.
@Composable
fun AppRoot(authState: AuthState, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        // consumeWindowInsets: the screens' imePadding() then adds only the part of the keyboard
        // that this padding (status and navigation bars) doesn't already cover
        val contentModifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
        when (authState) {
            // MainActivity keeps the splash screen up meanwhile, so there's nothing to show
            AuthState.Loading -> Unit
            AuthState.LoggedIn -> AppNavigation(startKey = Home, modifier = contentModifier)
            AuthState.LoggedOut -> AppNavigation(startKey = Login, modifier = contentModifier)
        }
    }
}
