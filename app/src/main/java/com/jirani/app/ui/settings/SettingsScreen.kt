package com.jirani.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.ui.reporting.ScreenTitle
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            title = "Settings",
            subtitle = "Security preferences stay local-first and should later be encrypted at rest.",
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Calculator Decoy Code", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Enter the code in the calculator to return to Jirani. No visible exit button is shown in decoy mode.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        OutlinedTextField(
            value = uiState.code,
            onValueChange = viewModel::updateCode,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New code") },
            placeholder = { Text("2468=") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
        OutlinedTextField(
            value = uiState.confirmCode,
            onValueChange = viewModel::updateConfirmCode,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm code") },
            placeholder = { Text("2468=") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
        Button(
            onClick = viewModel::saveCode,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Discreet Code")
        }
        Text(
            text = uiState.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    JiraniTheme {
        SettingsScreen()
    }
}
