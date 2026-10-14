package com.leireomadina.travelplanner.ui.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
) {
    // Like the web form's `required` fields: both must be filled in to submit
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
