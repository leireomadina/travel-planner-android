package com.leireomadina.travelplanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leireomadina.travelplanner.data.auth.AuthRepository
import com.leireomadina.travelplanner.data.auth.AuthState
import com.leireomadina.travelplanner.ui.navigation.AppRoot
import com.leireomadina.travelplanner.ui.theme.TravelPlannerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // The app was closed and the confirmation link started it. Only on a fresh start:
        // after a rotation the same intent comes back, and it was already handled.
        if (savedInstanceState == null) authRepository.handleConfirmationLink(intent)

        // Keeps the splash up while the saved session is read, so a logged-in user goes
        // straight to Home without a flash of Login
        splashScreen.setKeepOnScreenCondition { viewModel.authState.value == AuthState.Loading }

        enableEdgeToEdge()
        setContent {
            TravelPlannerTheme {
                val authState by viewModel.authState.collectAsStateWithLifecycle()
                AppRoot(authState = authState)
            }
        }
    }

    // The app was already open (launchMode singleTask), and the link brought it back
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        authRepository.handleConfirmationLink(intent)
    }
}
