package com.jirani.app.ui.reporting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.jirani.app.domain.agent.ReportingAgent
import com.jirani.app.domain.agent.SafetyReportGuidance
import com.jirani.app.domain.agent.SafetyReportRequest
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun ReportingScreen(
    modifier: Modifier = Modifier,
    reportingAgent: ReportingAgent = remember { ReportingAgent() },
) {
    var report by rememberSaveable { mutableStateOf("") }
    var guidance by remember { mutableStateOf<SafetyReportGuidance?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            title = "Anonymous Reporting",
            subtitle = "Capture safety alerts without names, phone numbers, or personal identifiers.",
        )
        OutlinedTextField(
            value = report,
            onValueChange = { report = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            label = { Text("Incident details") },
            placeholder = { Text("General location, time window, and observed risk.") },
            maxLines = 6,
        )
        Button(
            onClick = { guidance = reportingAgent.process(SafetyReportRequest(report)) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Prepare Verification Guidance")
        }
        guidance?.let { ReportingGuidancePanel(it) }
    }
}

@Composable
private fun ReportingGuidancePanel(guidance: SafetyReportGuidance) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(guidance.incidentSummary, fontWeight = FontWeight.SemiBold)
            Text("Threat type: ${guidance.threatType}")
            Text("Safe next steps", fontWeight = FontWeight.SemiBold)
            guidance.safeNextSteps.forEach { Text("- $it") }
            if (guidance.missingNonIdentifyingDetails.isNotEmpty()) {
                Text("Missing non-identifying details", fontWeight = FontWeight.SemiBold)
                guidance.missingNonIdentifyingDetails.forEach { Text("- $it") }
            }
        }
    }
}

@Composable
internal fun ScreenTitle(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportingScreenPreview() {
    JiraniTheme {
        ReportingScreen()
    }
}
