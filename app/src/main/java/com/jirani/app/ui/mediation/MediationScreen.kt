package com.jirani.app.ui.mediation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun MediationScreen(
    modifier: Modifier = Modifier,
    viewModel: MediationViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val chips = listOf(
        "Water Access" to "There is tension about shared water access.",
        "Grazing Rights" to "Families disagree about grazing access near the boundary.",
        "Border Crossing" to "People are worried about safe border crossing.",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Mediation", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            text = "Chat-style de-escalation with local neutralizer guidance.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToneCheckCard(uiState.neutralizedText)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 12.dp),
        ) {
            items(chips) { chip ->
                AssistChip(
                    onClick = { viewModel.useChip(chip.second) },
                    label = { Text(chip.first) },
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(uiState.messages) { message ->
                ChatBubble(message)
            }
        }
        OutlinedTextField(
            value = uiState.input,
            onValueChange = viewModel::updateInput,
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp),
            label = { Text("Conflict description") },
            placeholder = { Text("Use neutral facts. Avoid names or phone numbers.") },
            maxLines = 4,
        )
        Button(
            onClick = viewModel::submit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generate Neutralized Guidance")
        }
    }
}

@Composable
private fun ToneCheckCard(neutralizedText: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Tone Check",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Neutralized: $neutralizedText",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val fromUser = message.sender == "You"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.84f),
            color = if (fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = message.body,
                    color = if (fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MediationScreenPreview() {
    JiraniTheme {
        MediationScreen()
    }
}
