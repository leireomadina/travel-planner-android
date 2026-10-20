package com.leireomadina.travelplanner.ui.register

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
fun RegisterRoute(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRegisterClick = viewModel::onRegisterClick,
        onLoginClick = onLoginClick,
        modifier = modifier,
    )
}

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
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
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("register-title"),
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
                .testTag("register-email"),
        )
        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.password_label)) },
            placeholder = { Text(stringResource(R.string.password_placeholder)) },
            enabled = !uiState.isLoading,
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            // Always shown, so the rule is known before typing, not only after a failed submit
            supportingText = {
                Text(
                    stringResource(
                        R.string.password_min_length,
                        RegisterUiState.MIN_PASSWORD_LENGTH,
                    ),
                )
            },
            // Red once something is typed but it's still too short
            isError = uiState.password.isNotEmpty() && !uiState.isPasswordLongEnough,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onRegisterClick() }),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register-password"),
        )
        Button(
            onClick = onRegisterClick,
            enabled = uiState.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register-submit"),
        ) {
            Text(stringResource(R.string.register_submit))
        }
        TextButton(
            onClick = onLoginClick,
            enabled = !uiState.isLoading,
            modifier = Modifier.testTag("register-go-to-login"),
        ) {
            Text(stringResource(R.string.register_go_to_login))
        }
        // Only one of these shows at a time, like the web's v-if / v-else-if chain
        when {
            uiState.isLoading -> LoadingSpinner(
                contentDescription = stringResource(R.string.register_loading),
            )
            uiState.isSuccess -> Text(
                text = stringResource(R.string.register_successful),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag("register-successful")
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
            uiState.hasError -> Text(
                text = stringResource(R.string.register_error),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag("register-error")
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenEmptyPreview() {
    TravelPlannerTheme {
        RegisterScreen(RegisterUiState(), {}, {}, {}, {})
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenLoadingPreview() {
    TravelPlannerTheme {
        RegisterScreen(
            RegisterUiState(email = "test@test.com", password = "password", isLoading = true),
            {},
            {},
            {},
            {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenSuccessPreview() {
    TravelPlannerTheme {
        RegisterScreen(
            RegisterUiState(email = "test@test.com", password = "password", isSuccess = true),
            {},
            {},
            {},
            {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenErrorPreview() {
    TravelPlannerTheme {
        RegisterScreen(
            RegisterUiState(email = "test@test.com", password = "password", hasError = true),
            {},
            {},
            {},
            {},
        )
    }
}
