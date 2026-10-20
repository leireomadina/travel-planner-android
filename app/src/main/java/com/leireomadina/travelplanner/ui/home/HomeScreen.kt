package com.leireomadina.travelplanner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leireomadina.travelplanner.R
import com.leireomadina.travelplanner.ui.components.LoadingSpinner
import com.leireomadina.travelplanner.ui.theme.TravelPlannerTheme

@Composable
fun HomeRoute(modifier: Modifier = Modifier, viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onLogoutClick = viewModel::onLogoutClick,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(uiState: HomeUiState, onLogoutClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(stringResource(R.string.hello))
        Button(
            onClick = onLogoutClick,
            enabled = !uiState.isLoading,
            modifier = Modifier.testTag("logout-submit"),
        ) {
            Text(
                stringResource(
                    if (uiState.isLoading) R.string.logout_loading else R.string.logout_submit,
                ),
            )
        }
        if (uiState.isLoading) {
            LoadingSpinner(contentDescription = stringResource(R.string.logout_loading))
        }
        if (uiState.hasError) {
            Text(
                text = stringResource(R.string.logout_error),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .testTag("logout-error")
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TravelPlannerTheme {
        HomeScreen(HomeUiState(), {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoggingOutPreview() {
    TravelPlannerTheme {
        HomeScreen(HomeUiState(isLoading = true), {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    TravelPlannerTheme {
        HomeScreen(HomeUiState(hasError = true), {})
    }
}
