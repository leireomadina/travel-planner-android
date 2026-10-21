package com.leireomadina.travelplanner.ui.register

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leireomadina.travelplanner.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    // Survives Android killing the app in the background, unlike the ViewModel itself
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // The typed email comes back after the app is killed and restored. The password is never
    // saved: saved state is written to disk.
    private val _uiState = MutableStateFlow(
        RegisterUiState(email = savedStateHandle[EMAIL_KEY] ?: ""),
    )
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // Editing a field hides the previous result, which was about the old input
    fun onEmailChange(email: String) {
        savedStateHandle[EMAIL_KEY] = email
        _uiState.update { it.copy(email = email, isSuccess = false, hasError = false) }
    }

    fun onPasswordChange(password: String) =
        _uiState.update { it.copy(password = password, isSuccess = false, hasError = false) }

    fun onRegisterClick() {
        val state = _uiState.value
        if (!state.canSubmit) return

        // Set before launching, so the button is disabled at once and can't be tapped twice
        _uiState.update { it.copy(isLoading = true, isSuccess = false, hasError = false) }
        viewModelScope.launch {
            try {
                // Keyboards' autocomplete often adds a trailing space, which Supabase rejects
                authRepository.register(state.email.trim(), state.password)
                _uiState.update { it.copy(isSuccess = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(hasError = true) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private companion object {
        const val EMAIL_KEY = "email"
    }
}
