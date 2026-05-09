package com.jirani.app.ui.mediation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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
import com.jirani.app.ui.common.CalmModeCard
import com.jirani.app.ui.common.ChoiceCard
import com.jirani.app.ui.common.QuickExitButton
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun MediationScreen(
    modifier: Modifier = Modifier,
    viewModel: MediationViewModel = viewModel(),
    onQuickExit: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val conflictTypes = listOf("Water", "Land", "Grazing", "Family", "Neighbor", "Other")
    val incidentTypes = listOf("Access blocked", "Threats made", "Property damaged", "Agreement broken", "Not sure")
    val helpNeeds = listOf("Respond calmly", "Talk to them", "Create agreement", "Find safe next step")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ScreenIntro(modifier = Modifier.weight(1f))
                QuickExitButton(onClick = onQuickExit)
            }
        }
        item { CalmModeCard() }
        item {
            GuidedSection(
                title = "1. Choose the concern",
                options = conflictTypes,
                selected = uiState.conflictType,
                columns = 2,
                onSelect = viewModel::selectConflictType,
            )
        }
        item {
            GuidedSection(
                title = "2. What happened?",
                options = incidentTypes,
                selected = uiState.incidentType,
                columns = 1,
                onSelect = viewModel::selectIncidentType,
            )
        }
        item {
            GuidedSection(
                title = "3. What help do you need?",
                options = helpNeeds,
                selected = uiState.helpNeed,
                columns = 1,
                onSelect = viewModel::selectHelpNeed,
            )
        }
        item {
            OutlinedTextField(
                value = uiState.input,
                onValueChange = viewModel::updateInput,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Add anything else") },
                placeholder = { Text("No names or phone numbers.") },
                minLines = 2,
                maxLines = 3,
            )
        }
        item {
            Button(
                onClick = viewModel::submit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Help me respond calmly")
            }
        }
        item {
            AnimatedVisibility(visible = uiState.showToneCheck) {
                ToneCheckCard(
                    neutralizedText = uiState.neutralizedText,
                    onUse = viewModel::useNeutralizedText,
                )
            }
        }
        items(uiState.messages) { message ->
            ChatBubble(message)
        }
    }
}

@Composable
private fun ScreenIntro(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Resolve", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            text = "Step by step support.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GuidedSection(
    title: String,
    options: List<String>,
    selected: String,
    columns: Int,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (columns == 2) {
            options.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { option ->
                        ChoiceCard(
                            label = option,
                            selected = selected == option,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelect(option) },
                        )
                    }
                    if (row.size == 1) {
                        Column(modifier = Modifier.weight(1f)) {}
                    }
                }
            }
        } else {
            options.forEach { option ->
                ChoiceCard(
                    label = option,
                    selected = selected == option,
                    onClick = { onSelect(option) },
                )
            }
        }
    }
}

@Composable
private fun ToneCheckCard(
    neutralizedText: String,
    onUse: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Tone Check",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = neutralizedText,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(onClick = onUse) {
                Text("Use Safe Version")
            }
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
            border = if (fromUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
