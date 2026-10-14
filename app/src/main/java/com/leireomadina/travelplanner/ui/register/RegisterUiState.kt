package com.leireomadina.travelplanner.ui.register

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    // Sign-up worked and Supabase sent the confirmation email
    val isSuccess: Boolean = false,
    val hasError: Boolean = false,
) {
    // Like the web form's `required` fields: both must be filled in to submit.
    // After a sign-up works, submitting again would only send another confirmation
    // email, so it stays off until a field is edited.
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading && !isSuccess
}
