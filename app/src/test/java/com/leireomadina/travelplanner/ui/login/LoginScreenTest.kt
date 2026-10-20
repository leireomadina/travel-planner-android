package com.leireomadina.travelplanner.ui.login

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leireomadina.travelplanner.R
import com.leireomadina.travelplanner.ui.theme.TravelPlannerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Draws LoginScreen with a given state and checks what it shows. Runs on the computer with
// Robolectric; elements are found by their testTag, like data-cy in the web's Cypress tests.
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `sign in is disabled until both fields are filled in`() {
        show(LoginUiState(email = "test@test.com"))

        composeRule.onNodeWithTag("login-submit").assertIsNotEnabled()
    }

    @Test
    fun `sign in is enabled with both fields filled in, and tapping it logs in`() {
        var loginClicked = false
        show(
            LoginUiState(email = "test@test.com", password = "password"),
            onLoginClick = { loginClicked = true },
        )

        composeRule.onNodeWithTag("login-submit").assertIsEnabled().performClick()

        assertTrue(loginClicked)
    }

    @Test
    fun `wrong credentials show the wrong-password message`() {
        show(LoginUiState(error = LoginError.WrongCredentials))

        composeRule.onNodeWithTag("login-error")
            .assertIsDisplayed()
            .assertTextEquals(context.getString(R.string.login_error))
    }

    @Test
    fun `a network error shows the connection message`() {
        show(LoginUiState(error = LoginError.Network))

        composeRule.onNodeWithTag("login-error")
            .assertTextEquals(context.getString(R.string.login_network_error))
    }

    @Test
    fun `no error message without an error`() {
        show(LoginUiState())

        composeRule.onNodeWithTag("login-error").assertDoesNotExist()
    }

    private fun show(uiState: LoginUiState, onLoginClick: () -> Unit = {}) {
        composeRule.setContent {
            TravelPlannerTheme {
                LoginScreen(
                    uiState = uiState,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onLoginClick = onLoginClick,
                    onRegisterClick = {},
                )
            }
        }
    }
}
