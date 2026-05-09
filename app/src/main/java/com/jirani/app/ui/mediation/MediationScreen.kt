package com.jirani.app.ui.mediation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jirani.app.domain.agent.MediationAgent
import com.jirani.app.domain.agent.MediationGuidance
import com.jirani.app.domain.agent.MediationRequest
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun MediationScreen(
    modifier: Modifier = Modifier,
    mediationAgent: MediationAgent = remember { MediationAgent() },
) {
    var description by rememberSaveable { mutableStateOf("") }
    var guidance by remember { mutableStateOf<MediationGuidance?>(null) }

    Column(
        modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Mediation",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Neutralizer suggestions for conflict de-escalation.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ToneCheckCard(description)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = { description = "There is tension about shared water access." },
                label = { Text("Water Rights") },
            )
            AssistChip(
                onClick = { description = "Families disagree about grazing access near the boundary." },
                label = { Text("Grazing Access") },
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            label = { Text("Conflict description") },
            placeholder = { Text("Example: Access to the shared well is blocked.") },
            maxLines = 6,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = {
                    guidance = mediationAgent.process(MediationRequest(description))
                },
            ) {
                Text("Generate Guidance")
            }
        }

        guidance?.let {
            GuidancePanel(guidance = it)
        }
    }
}

@Composable
private fun ToneCheckCard(description: String) {
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
                text = if (description.isBlank()) {
                    "Safe version appears here as you prepare a message."
                } else {
                    "Safe version: We need a calm discussion to clarify facts and agree on fair next steps."
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun GuidancePanel(guidance: MediationGuidance) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GuidanceSection(title = "Summary", body = guidance.summary)
            GuidanceSection(title = "Concerns", items = guidance.concerns)
            GuidanceSection(title = "Recommendations", items = guidance.recommendations)
            GuidanceSection(title = "Next Step", body = guidance.nextStep)
            guidance.safetyNote?.let {
                GuidanceSection(title = "Safety", body = it)
            }
        }
    }
}

@Composable
private fun GuidanceSection(
    title: String,
    body: String,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun GuidanceSection(
    title: String,
    items: List<String>,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        items.forEach { item ->
            Text(
                text = "- $item",
                style = MaterialTheme.typography.bodyMedium,
            )
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
