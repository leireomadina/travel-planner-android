package com.leireomadina.travelplanner.ui.register

import com.leireomadina.travelplanner.data.auth.AuthState
import com.leireomadina.travelplanner.data.auth.FakeAuthRepository
import com.leireomadina.travelplanner.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

// advanceUntilIdle() is still marked experimental in kotlinx-coroutines-test
@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository()
    private val viewModel = RegisterViewModel(authRepository)

    @Test
    fun `register shows the loading state until it finishes`() = runTest {
        fillIn(password = "password")

        viewModel.onRegisterClick()
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful sign-up asks to check the email and stays logged out`() = runTest {
        fillIn(password = "password")

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.hasError)
        assertEquals(AuthState.LoggedOut, authRepository.authState.value)
    }

    @Test
    fun `failed sign-up shows an error`() = runTest {
        fillIn(password = FakeAuthRepository.WRONG_PASSWORD)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
        assertFalse(viewModel.uiState.value.isSuccess)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `can't submit again after a successful sign-up`() = runTest {
        fillIn(password = "password")
        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `editing a field hides the previous result and allows submitting again`() = runTest {
        fillIn(password = "password")
        viewModel.onRegisterClick()
        advanceUntilIdle()

        viewModel.onEmailChange("other@test.com")

        assertFalse(viewModel.uiState.value.isSuccess)
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `editing a field hides the previous error`() = runTest {
        fillIn(password = FakeAuthRepository.WRONG_PASSWORD)
        viewModel.onRegisterClick()
        advanceUntilIdle()

        viewModel.onPasswordChange("password")

        assertFalse(viewModel.uiState.value.hasError)
    }

    @Test
    fun `spaces around the email are removed before signing up`() = runTest {
        viewModel.onEmailChange(" test@test.com ")
        viewModel.onPasswordChange("password")

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertEquals("test@test.com", authRepository.lastEmail)
    }

    @Test
    fun `empty fields can't be submitted`() = runTest {
        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.canSubmit)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isSuccess)
    }

    private fun fillIn(password: String) {
        viewModel.onEmailChange("test@test.com")
        viewModel.onPasswordChange(password)
    }
}
