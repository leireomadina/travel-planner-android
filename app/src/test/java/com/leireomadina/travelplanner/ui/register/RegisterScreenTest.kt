package com.leireomadina.travelplanner.ui.register

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leireomadina.travelplanner.ui.theme.TravelPlannerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // The text field marks itself as being in error for accessibility, which tests can read
    private val isInError = SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)

    @Test
    fun `a too-short password marks the field red and keeps the button disabled`() {
        show(RegisterUiState(email = "test@test.com", password = "123"))

        composeRule.onNodeWithTag("register-password").assert(isInError)
        composeRule.onNodeWithTag("register-submit").assertIsNotEnabled()
    }

    @Test
    fun `a long enough password enables the button`() {
        show(RegisterUiState(email = "test@test.com", password = "123456"))

        composeRule.onNodeWithTag("register-password").assert(!isInError)
        composeRule.onNodeWithTag("register-submit").assertIsEnabled()
    }

    @Test
    fun `an empty password isn't marked red before typing`() {
        show(RegisterUiState())

        composeRule.onNodeWithTag("register-password").assert(!isInError)
    }

    @Test
    fun `after a successful sign-up the message shows and the button is disabled`() {
        show(RegisterUiState(email = "test@test.com", password = "123456", isSuccess = true))

        composeRule.onNodeWithTag("register-successful").assertIsDisplayed()
        composeRule.onNodeWithTag("register-submit").assertIsNotEnabled()
    }

    @Test
    fun `an error shows the error message`() {
        show(RegisterUiState(hasError = true))

        composeRule.onNodeWithTag("register-error").assertIsDisplayed()
        composeRule.onNodeWithTag("register-successful").assertDoesNotExist()
    }

    @Test
    fun `the log in link goes to login`() {
        var loginClicked = false
        show(RegisterUiState(), onLoginClick = { loginClicked = true })

        composeRule.onNodeWithTag("register-go-to-login").performClick()

        assertTrue(loginClicked)
    }

    private fun show(uiState: RegisterUiState, onLoginClick: () -> Unit = {}) {
        composeRule.setContent {
            TravelPlannerTheme {
                RegisterScreen(
                    uiState = uiState,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onRegisterClick = {},
                    onLoginClick = onLoginClick,
                )
            }
        }
    }
}
