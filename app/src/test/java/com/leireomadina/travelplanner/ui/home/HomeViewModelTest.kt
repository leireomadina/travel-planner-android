package com.leireomadina.travelplanner.ui.home

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
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository = FakeAuthRepository(initialState = AuthState.LoggedIn)
    private val viewModel = HomeViewModel(authRepository)

    @Test
    fun `logout shows the loading state until it finishes`() = runTest {
        viewModel.onLogoutClick()
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful logout logs the user out without an error`() = runTest {
        viewModel.onLogoutClick()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
        assertEquals(AuthState.LoggedOut, authRepository.authState.value)
    }

    @Test
    fun `failed logout shows an error and stays logged in`() = runTest {
        authRepository.shouldFailLogout = true

        viewModel.onLogoutClick()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(AuthState.LoggedIn, authRepository.authState.value)
    }
}
