package com.leireomadina.travelplanner.data.auth

import android.content.Intent
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow

// In-memory AuthRepository for tests: no network, and any password except WRONG_PASSWORD works.
class FakeAuthRepository(initialState: AuthState = AuthState.LoggedOut) : AuthRepository {

    override val authState = MutableStateFlow(initialState)

    // Set to true to make logout() fail, e.g. to test the logout error message
    var shouldFailLogout = false

    // Set to true to make every call fail as if offline, like supabase-kt's HttpRequestException
    var isOffline = false

    // The email the last login() or register() call received
    var lastEmail: String? = null
        private set

    override suspend fun login(email: String, password: String) {
        lastEmail = email
        if (isOffline) throw IOException("Offline")
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

    // The intents passed to handleConfirmationLink, so tests can check the app hands
    // confirmation links over. Exchanging the code with Supabase is checked by hand on a device.
    val confirmationLinks = mutableListOf<Intent>()

    override fun handleConfirmationLink(intent: Intent) {
        confirmationLinks += intent
    }

    companion object {
        // Long enough for the register form's minimum, so register tests can submit it
        const val WRONG_PASSWORD = "wrong-password"
    }
}
