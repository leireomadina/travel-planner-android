package com.leireomadina.travelplanner.data.auth

import kotlinx.coroutines.flow.MutableStateFlow

// In-memory AuthRepository for tests: no network, and any password except "wrong" works.
class FakeAuthRepository(
    initialState: AuthState = AuthState.LoggedOut,
) : AuthRepository {

    override val authState = MutableStateFlow(initialState)

    override suspend fun login(email: String, password: String) {
        if (password == WRONG_PASSWORD) throw IllegalStateException("Invalid login credentials")
        authState.value = AuthState.LoggedIn
    }

    // Like Supabase with email confirmation on: the user stays logged out until they
    // confirm their email.
    override suspend fun register(email: String, password: String) {
        if (password == WRONG_PASSWORD) throw IllegalStateException("Sign-up failed")
    }

    override suspend fun logout() {
        authState.value = AuthState.LoggedOut
    }

    companion object {
        const val WRONG_PASSWORD = "wrong"
    }
}
