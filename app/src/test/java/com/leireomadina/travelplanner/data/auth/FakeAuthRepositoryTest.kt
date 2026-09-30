package com.leireomadina.travelplanner.data.auth

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAuthRepositoryTest {

    private val repository = FakeAuthRepository()

    @Test
    fun `login with a valid password logs the user in`() = runTest {
        repository.login("test@test.com", "password")

        assertEquals(AuthState.LoggedIn, repository.authState.value)
    }

    @Test
    fun `login with a wrong password throws and stays logged out`() = runTest {
        val result = runCatching {
            repository.login("test@test.com", FakeAuthRepository.WRONG_PASSWORD)
        }

        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertEquals(AuthState.LoggedOut, repository.authState.value)
    }

    @Test
    fun `logout logs the user out`() = runTest {
        repository.login("test@test.com", "password")

        repository.logout()

        assertEquals(AuthState.LoggedOut, repository.authState.value)
    }
}
