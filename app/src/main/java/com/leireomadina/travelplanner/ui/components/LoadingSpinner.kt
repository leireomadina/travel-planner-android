package com.leireomadina.travelplanner.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

// Android version of the web app's LoadingSpinner.vue. contentDescription is what
// screen readers announce, like the web component's ariaLabel.
@Composable
fun LoadingSpinner(contentDescription: String, modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier
            .size(32.dp)
            .testTag("loading-spinner")
            .semantics { this.contentDescription = contentDescription },
    )
}
