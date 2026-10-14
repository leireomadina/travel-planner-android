package com.leireomadina.travelplanner.ui.login

import com.leireomadina.travelplanner.data.auth.AuthState
import com.leireomadina.travelplanner.data.auth.FakeAuthRepository
import com.leireomadina.travelplanner.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// advanceUntilIdle() is still marked experimental in kotlinx-coroutines-test
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val viewModel = LoginViewModel(authRepository)

    @Test
    fun `typing updates the email and password`() {
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange("password")

        assertEquals("test@test.com", viewModel.uiState.value.email)
        assertEquals("password", viewModel.uiState.value.password)
    }

    @Test
    fun `login shows the loading state until it finishes`() = runTest {
        fillIn(password = "password")

        viewModel.onLoginClick()
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful login logs the user in without an error`() = runTest {
        fillIn(password = "password")

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(AuthState.LoggedIn, authRepository.authState.value)
    }

    @Test
    fun `wrong password shows an error`() = runTest {
        fillIn(password = FakeAuthRepository.WRONG_PASSWORD)

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertEquals(LoginError.WrongCredentials, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(AuthState.LoggedOut, authRepository.authState.value)
    }

    @Test
    fun `being offline shows a connection error, not a wrong-password one`() = runTest {
        authRepository.isOffline = true
        fillIn(password = "password")

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertEquals(LoginError.Network, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `trying again clears the previous error`() = runTest {
        fillIn(password = FakeAuthRepository.WRONG_PASSWORD)
        viewModel.onLoginClick()
        advanceUntilIdle()

        viewModel.onPasswordChange("password")
        viewModel.onLoginClick()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `editing a field hides the previous error`() = runTest {
        fillIn(password = FakeAuthRepository.WRONG_PASSWORD)
        viewModel.onLoginClick()
        advanceUntilIdle()

        viewModel.onPasswordChange("password")

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `spaces around the email are removed before logging in`() = runTest {
        viewModel.onEmailChange(" test@test.com ")
        viewModel.onPasswordChange("password")

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertEquals("test@test.com", authRepository.lastEmail)
    }

    @Test
    fun `empty fields can't be submitted`() = runTest {
        viewModel.onLoginClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSubmit)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(AuthState.LoggedOut, authRepository.authState.value)
    }

    private fun fillIn(password: String) {
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange(password)
    }
}
