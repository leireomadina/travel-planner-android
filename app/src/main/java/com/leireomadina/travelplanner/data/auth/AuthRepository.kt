package com.leireomadina.travelplanner.data.auth

import kotlinx.coroutines.flow.Flow

// Single source of truth for the session. Mirrors the web app's src/stores/auth.ts:
// each function throws if Supabase returns an error.
interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun login(email: String, password: String)

    suspend fun register(email: String, password: String)

    suspend fun logout()
}
