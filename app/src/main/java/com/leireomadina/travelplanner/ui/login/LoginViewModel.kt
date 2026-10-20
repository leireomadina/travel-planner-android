package com.leireomadina.travelplanner.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leireomadina.travelplanner.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Editing a field hides the previous error, which was about the old input
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, error = null) }

    fun onPasswordChange(password: String) =
        _uiState.update { it.copy(password = password, error = null) }

    fun onLoginClick() {
        val state = _uiState.value
        if (!state.canSubmit) return

        // Set before launching, so the button is disabled at once and can't be tapped twice
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                // Keyboards' autocomplete often adds a trailing space, which Supabase rejects
                authRepository.login(state.email.trim(), state.password)
                // No navigation here: the session becomes LoggedIn and the app root shows Home.
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                _uiState.update { it.copy(error = LoginError.Network) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = LoginError.WrongCredentials) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
