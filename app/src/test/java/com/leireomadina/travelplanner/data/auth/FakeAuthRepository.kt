package com.leireomadina.travelplanner.data.auth

import kotlinx.coroutines.flow.MutableStateFlow

// In-memory AuthRepository for tests: no network, and any password except "wrong" works.
class FakeAuthRepository(
    initialState: AuthState = AuthState.LoggedOut,
) : AuthRepository {

    override val authState = MutableStateFlow(initialState)

    // Set to true to make logout() fail, e.g. to test the logout error message
    var shouldFailLogout = false

    // The email the last login() or register() call received
    var lastEmail: String? = null
        private set

    override suspend fun login(email: String, password: String) {
        lastEmail = email
        if (password == WRONG_PASSWORD) throw IllegalStateException("Invalid login credentials")
        authState.value = AuthState.LoggedIn
    }

    // Like Supabase with email confirmation on: the user stays logged out until they
    // confirm their email.
    override suspend fun register(email: String, password: String) {
        lastEmail = email
        if (password == WRONG_PASSWORD) throw IllegalStateException("Sign-up failed")
    }

    override suspend fun logout() {
        if (shouldFailLogout) throw IllegalStateException("Sign-out failed")
        authState.value = AuthState.LoggedOut
    }

    companion object {
        const val WRONG_PASSWORD = "wrong"
    }
}
