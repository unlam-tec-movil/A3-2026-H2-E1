package ar.edu.unlam.scaffoldingandroid3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MessageContent(
    message: String,
    title: String? = null,
    showProgress: Boolean = false,
    primaryLabel: String? = null,
    onPrimary: () -> Unit = {},
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showProgress) {
            CircularProgressIndicator()

            Spacer(
                modifier = Modifier.height(16.dp),
            )
        }

        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(
                modifier = Modifier.height(8.dp),
            )
        }

        Text(
            text = message,
            textAlign = TextAlign.Center,
        )

        if (primaryLabel != null) {
            Spacer(
                modifier = Modifier.height(16.dp),
            )

            Button(
                onClick = onPrimary,
            ) {
                Text(primaryLabel)
            }
        }

        if (secondaryLabel != null) {
            Spacer(
                modifier = Modifier.height(8.dp),
            )

            OutlinedButton(
                onClick = onSecondary,
            ) {
                Text(secondaryLabel)
            }
        }
    }
}
