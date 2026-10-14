package com.leireomadina.travelplanner.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leireomadina.travelplanner.R
import com.leireomadina.travelplanner.ui.components.LoadingSpinner
import com.leireomadina.travelplanner.ui.theme.TravelPlannerTheme

@Composable
fun LoginRoute(
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick,
        onRegisterClick = onRegisterClick,
        modifier = modifier,
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        // Scrolls, and leaves room for the keyboard, so the button and messages stay
        // reachable on small screens or with large text
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("login-title"),
        )
        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text(stringResource(R.string.email_label)) },
            placeholder = { Text(stringResource(R.string.email_placeholder)) },
            enabled = !uiState.isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login-email"),
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password_label)) },
            placeholder = { Text(stringResource(R.string.password_placeholder)) },
            enabled = !uiState.isLoading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onLoginClick() }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login-password"),
        )
        Button(
            onClick = onLoginClick,
            enabled = uiState.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login-submit"),
        ) {
            Text(stringResource(R.string.login_submit))
        }
        TextButton(
            onClick = onRegisterClick,
            enabled = !uiState.isLoading,
            modifier = Modifier.testTag("login-go-to-register"),
        ) {
            Text(stringResource(R.string.login_go_to_register))
        }
        if (uiState.isLoading) {
            LoadingSpinner(contentDescription = stringResource(R.string.login_loading))
        }
        if (uiState.hasError) {
            Text(
                text = stringResource(R.string.login_error),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                // Announced by screen readers when it appears, like the web's role="alert"
                modifier = Modifier
                    .testTag("login-error")
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenEmptyPreview() {
    TravelPlannerTheme {
        LoginScreen(LoginUiState(), {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenLoadingPreview() {
    TravelPlannerTheme {
        LoginScreen(
            LoginUiState(email = "test@test.com", password = "password", isLoading = true),
            {}, {}, {}, {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenErrorPreview() {
    TravelPlannerTheme {
        LoginScreen(
            LoginUiState(email = "test@test.com", password = "wrong", hasError = true),
            {}, {}, {}, {},
        )
    }
}
