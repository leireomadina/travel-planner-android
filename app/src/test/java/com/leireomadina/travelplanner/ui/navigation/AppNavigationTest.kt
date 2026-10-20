package com.leireomadina.travelplanner.ui.navigation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leireomadina.travelplanner.MainActivity
import com.leireomadina.travelplanner.data.auth.AuthRepository
import com.leireomadina.travelplanner.data.auth.AuthState
import com.leireomadina.travelplanner.data.auth.FakeAuthRepository
import com.leireomadina.travelplanner.di.AuthModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

// Runs the whole app (MainActivity, AppRoot, the real screens and ViewModels) with
// FakeAuthRepository in place of Supabase, and checks the flows between screens.
@HiltAndroidTest
@UninstallModules(AuthModule::class)
@Config(application = HiltTestApplication::class)
@RunWith(AndroidJUnit4::class)
class AppNavigationTest {

    // Whoever asks Hilt for an AuthRepository gets this fake instead of Supabase
    @BindValue
    @JvmField
    val authRepository: AuthRepository = FakeAuthRepository(initialState = AuthState.LoggedOut)

    private val fakeAuth get() = authRepository as FakeAuthRepository

    private val hiltRule = HiltAndroidRule(this)
    private val composeRule = createAndroidComposeRule<MainActivity>()

    // Hilt must be ready before the activity starts
    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(hiltRule).around(composeRule)

    @Test
    fun `a logged-out user starts on login`() {
        composeRule.onNodeWithTag("login-title").assertExists()
    }

    @Test
    fun `login opens register, and back returns to login`() {
        composeRule.onNodeWithTag("login-go-to-register").performClick()
        composeRule.onNodeWithTag("register-title").assertExists()

        pressBack()

        composeRule.onNodeWithTag("login-title").assertExists()
        composeRule.onNodeWithTag("register-title").assertDoesNotExist()
    }

    @Test
    fun `the log in link on register returns to login`() {
        composeRule.onNodeWithTag("login-go-to-register").performClick()

        composeRule.onNodeWithTag("register-go-to-login").performClick()

        composeRule.onNodeWithTag("login-title").assertExists()
    }

    @Test
    fun `logging in shows home`() {
        composeRule.onNodeWithTag("login-email").performTextInput("test@test.com")
        composeRule.onNodeWithTag("login-password").performTextInput("password")

        composeRule.onNodeWithTag("login-submit").performClick()

        composeRule.onNodeWithTag("logout-submit").assertExists()
        composeRule.onNodeWithTag("login-title").assertDoesNotExist()
    }

    @Test
    fun `a wrong password stays on login with an error`() {
        composeRule.onNodeWithTag("login-email").performTextInput("test@test.com")
        composeRule.onNodeWithTag(
            "login-password",
        ).performTextInput(FakeAuthRepository.WRONG_PASSWORD)

        composeRule.onNodeWithTag("login-submit").performClick()

        composeRule.onNodeWithTag("login-error").assertExists()
        composeRule.onNodeWithTag("logout-submit").assertDoesNotExist()
    }

    @Test
    fun `after logging out, back leaves the app instead of returning to home`() {
        logIn()
        composeRule.onNodeWithTag("logout-submit").performClick()
        composeRule.onNodeWithTag("login-title").assertExists()

        pressBack()

        // Login was the only screen left, so back closes the app
        assertTrue(composeRule.activity.isFinishing)
    }

    @Test
    fun `logging in replaces the login screens, so back from home leaves the app`() {
        composeRule.onNodeWithTag("login-go-to-register").performClick()
        logIn()

        pressBack()

        assertTrue(composeRule.activity.isFinishing)
    }

    // The session changing from outside a screen (e.g. the confirmation link) also
    // switches to Home
    private fun logIn() {
        fakeAuth.authState.value = AuthState.LoggedIn
        composeRule.onNodeWithTag("logout-submit").assertExists()
    }

    private fun pressBack() {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }
}
