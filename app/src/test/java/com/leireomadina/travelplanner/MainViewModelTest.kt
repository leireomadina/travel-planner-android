package com.leireomadina.travelplanner

import com.leireomadina.travelplanner.data.auth.AuthState
import com.leireomadina.travelplanner.data.auth.FakeAuthRepository
import com.leireomadina.travelplanner.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

// advanceUntilIdle() is still marked experimental in kotlinx-coroutines-test
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `starts loading, so the splash screen stays up`() {
        val authRepository = FakeAuthRepository(initialState = AuthState.Loading)
        val viewModel = MainViewModel(authRepository)

        assertEquals(AuthState.Loading, viewModel.authState.value)
    }

    @Test
    fun `follows the session as it logs in and out`() = runTest {
        val authRepository = FakeAuthRepository(initialState = AuthState.LoggedOut)
        val viewModel = MainViewModel(authRepository)
        advanceUntilIdle()
        assertEquals(AuthState.LoggedOut, viewModel.authState.value)

        authRepository.login("test@test.com", "password")
        advanceUntilIdle()
        assertEquals(AuthState.LoggedIn, viewModel.authState.value)

        authRepository.logout()
        advanceUntilIdle()
        assertEquals(AuthState.LoggedOut, viewModel.authState.value)
    }
}
