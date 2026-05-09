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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
    mediationAgent: MediationAgent = remember { MediationAgent() },
) {
    var description by rememberSaveable { mutableStateOf("") }
    var guidance by remember { mutableStateOf<MediationGuidance?>(null) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Jirani",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Mediation Assistant",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

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
}

@Composable
private fun GuidancePanel(guidance: MediationGuidance) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
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
