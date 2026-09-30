package com.leireomadina.travelplanner.data.auth

sealed interface AuthState {
    // The saved session is still being read from the device
    data object Loading : AuthState

    data object LoggedIn : AuthState

    data object LoggedOut : AuthState
}
