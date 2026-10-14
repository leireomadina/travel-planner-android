package com.leireomadina.travelplanner.ui.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    // Why the last login failed, or null if it didn't
    val error: LoginError? = null,
) {
    // Like the web form's `required` fields: both must be filled in to submit
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

enum class LoginError {
    // Supabase answered and rejected the login (in practice, a wrong email or password)
    WrongCredentials,

    // Supabase couldn't be reached, so the credentials may well be right
    Network,
}
