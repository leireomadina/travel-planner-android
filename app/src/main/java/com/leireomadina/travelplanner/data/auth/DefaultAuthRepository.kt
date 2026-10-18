package com.leireomadina.travelplanner.data.auth

import android.content.Intent
import android.util.Log
import com.leireomadina.travelplanner.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class DefaultAuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
) : AuthRepository {

    override val authState: Flow<AuthState> = supabase.auth.sessionStatus.map { status ->
        when (status) {
            is SessionStatus.Initializing -> AuthState.Loading
            is SessionStatus.Authenticated -> AuthState.LoggedIn
            is SessionStatus.NotAuthenticated -> AuthState.LoggedOut
            // The saved session couldn't be refreshed (e.g. offline). supabase-kt keeps
            // retrying, so the user stays logged in.
            is SessionStatus.RefreshFailure -> AuthState.LoggedIn
        }
    }

    override suspend fun login(email: String, password: String) = logFailure("Login") {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun register(email: String, password: String) = logFailure("Sign-up") {
        supabase.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        // signUpWith returns the new user, which nothing here needs
        Unit
    }

    override suspend fun logout() = logFailure("Logout") {
        supabase.auth.signOut()
    }

    // The link carries the new session in its #fragment. Importing it sets the session
    // status to Authenticated, so authState becomes LoggedIn and the app shows Home.
    override fun handleConfirmationLink(intent: Intent) {
        supabase.handleDeeplinks(intent, onError = { logError("Confirmation link", it) })
    }

    // The screens only show a generic message, so in debug builds this logs Supabase's
    // actual reason (e.g. "HTTP 429 over_email_send_rate_limit") to Logcat under the
    // AuthRepository tag. The error is then rethrown for the ViewModel to handle as before.
    private inline fun <T> logFailure(action: String, block: () -> T): T =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logError(action, e)
            throw e
        }

    private fun logError(action: String, error: Throwable) {
        if (!BuildConfig.DEBUG) return
        // Only the status and Supabase's error code and text: never the email or password
        val reason = when (error) {
            is RestException -> "HTTP ${error.statusCode} ${error.error}: ${error.description}"
            else -> error.toString()
        }
        Log.w(TAG, "$action failed: $reason")
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
