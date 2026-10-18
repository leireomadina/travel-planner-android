package com.leireomadina.travelplanner.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// One key per screen, like the web app's routes. @Serializable lets the back stack
// be saved and restored when Android kills the app in the background.

@Serializable
data object Login : NavKey

@Serializable
data object Register : NavKey

@Serializable
data object Home : NavKey
