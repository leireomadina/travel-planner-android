package com.leireomadina.travelplanner.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.leireomadina.travelplanner.ui.home.HomeRoute
import com.leireomadina.travelplanner.ui.login.LoginRoute
import com.leireomadina.travelplanner.ui.register.RegisterRoute

// The screens reachable from startKey. The back stack is a plain list: adding a key
// opens that screen, and system back removes the last one.
@Composable
fun AppNavigation(
    startKey: NavKey,
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(startKey)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryDecorators = listOf(
            // Keeps each screen's remembered UI state (e.g. scroll position) while it's
            // in the back stack
            rememberSaveableStateHolderNavEntryDecorator(),
            // Gives each screen its own ViewModels, cleared when it leaves the back stack,
            // so going back to Login and then to Register starts with an empty form
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Login> {
                Screen { LoginRoute(onRegisterClick = { backStack.add(Register) }) }
            }
            entry<Register> {
                // Register is only opened from Login, so going "to Login" is going back.
                // Adding Login instead would stack [Login, Register, Login].
                Screen { RegisterRoute(onLoginClick = { backStack.removeLastOrNull() }) }
            }
            entry<Home> {
                Screen { HomeRoute() }
            }
        },
    )
}

// Gives a screen a solid background in the theme's color. Without it screens are see-through,
// so during the back animation the screen in front shows the one behind through it.
@Composable
private fun Screen(content: @Composable () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        content()
    }
}
