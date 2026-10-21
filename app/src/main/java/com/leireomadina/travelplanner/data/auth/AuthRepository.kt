package com.leireomadina.travelplanner.data.auth

import android.content.Intent
import kotlinx.coroutines.flow.Flow

// Single source of truth for the session. Mirrors the web app's src/stores/auth.ts:
// each function throws if Supabase returns an error, or an IOException if Supabase
// can't be reached (e.g. offline).
interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun login(email: String, password: String)

    suspend fun register(email: String, password: String)

    suspend fun logout()

    // Logs the user in from the confirmation email's link (travelplanner://confirm?code=…),
    // if the intent carries one. Other intents are ignored.
    fun handleConfirmationLink(intent: Intent)
}
