package com.jirani.app.ui.mediation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.R
import com.jirani.app.ui.common.QuickExitButton
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun MediationScreen(
    modifier: Modifier = Modifier,
    viewModel: MediationViewModel = viewModel(),
    onQuickExit: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topics = listOf("Water", "Land", "Grazing", "Family", "Neighbor", "Safety")
    val recentGuidance = listOf(
        RecentGuidance("Water access concern", "Our neighbors blocked access to the water point."),
        RecentGuidance("Grazing route disagreement", "Families disagree about grazing access near the boundary."),
        RecentGuidance("Broken agreement", "An earlier agreement was not followed and tension is rising."),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 4.dp),
        ) {
            item { ResolveHero(onQuickExit = onQuickExit) }
            item { CalmAssistCard(onUse = { viewModel.useChip("I need help responding calmly.") }) }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 8.dp),
                ) {
                    items(topics) { topic ->
                        AssistChip(
                            onClick = { viewModel.useChip("$topic concern") },
                            label = { Text(topic) },
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Recent guidance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Local", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
            items(recentGuidance) { guidance ->
                RecentGuidanceCard(
                    guidance = guidance,
                    onClick = { viewModel.useRecentGuidance(guidance.prompt) },
                )
            }
            if (uiState.messages.size > 1) {
                item {
                    Text("Conversation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                items(uiState.messages) { message ->
                    ChatBubble(message)
                }
            }
        }
        AnimatedVisibility(visible = uiState.showToneCheck) {
            ToneCheckCard(
                neutralizedText = uiState.neutralizedText,
                onUse = viewModel::useNeutralizedText,
            )
        }
        ChatComposer(
            input = uiState.input,
            onInputChange = viewModel::updateInput,
            onSend = viewModel::submit,
        )
    }
}

@Composable
private fun ResolveHero(onQuickExit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Hi, I'm Jirani", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = "What happened?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Share only what feels safe. No names needed.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        QuickExitButton(onClick = onQuickExit)
    }
}

@Composable
private fun CalmAssistCard(onUse: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_nav_mediation),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("Calm response", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Get neutral wording and a safe next step.", style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedButton(onClick = onUse) {
                Text("Use")
            }
        }
    }
}

@Composable
private fun RecentGuidanceCard(
    guidance: RecentGuidance,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_nav_mediation),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(guidance.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    guidance.prompt,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text("Open", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ChatComposer(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp),
                label = { Text("Message Jirani") },
                placeholder = { Text("Example: Our neighbors blocked the water point.") },
                minLines = 2,
                maxLines = 4,
            )
            Button(
                onClick = onSend,
                modifier = Modifier.fillMaxWidth(),
                enabled = input.isNotBlank(),
            ) {
                Text("Send")
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
            modifier = Modifier.padding(12.dp),
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
                color = MaterialTheme.colorScheme.onSurface,
            )
            OutlinedButton(onClick = onUse) {
                Text("Use safe version")
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
            modifier = Modifier.fillMaxWidth(if (fromUser) 0.82f else 0.9f),
            color = if (fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            border = if (fromUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
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
