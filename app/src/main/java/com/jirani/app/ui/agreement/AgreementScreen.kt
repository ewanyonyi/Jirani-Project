package com.jirani.app.ui.agreement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.jirani.app.domain.agent.AgreementSummary
import com.jirani.app.domain.agent.AgreementSummaryRequest
import com.jirani.app.domain.agent.SummaryAgent
import com.jirani.app.ui.reporting.ScreenTitle
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun AgreementScreen(
    modifier: Modifier = Modifier,
    summaryAgent: SummaryAgent = remember { SummaryAgent() },
) {
    var issue by rememberSaveable { mutableStateOf("") }
    var commitments by rememberSaveable { mutableStateOf("") }
    var summary by remember { mutableStateOf<AgreementSummary?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            title = "Vault",
            subtitle = "Local library of draft, signed, and pending-sync records.",
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "Encrypted local vault: ready for Room/SQLite",
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Local search") },
            placeholder = { Text("Search saved agreements") },
            enabled = false,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("Signed") })
            AssistChip(onClick = {}, label = { Text("Draft") })
            AssistChip(onClick = {}, label = { Text("Synced") })
        }
        OutlinedTextField(
            value = issue,
            onValueChange = { issue = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Issue") },
            placeholder = { Text("Example: Shared water pump access") },
        )
        OutlinedTextField(
            value = commitments,
            onValueChange = { commitments = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            label = { Text("Commitments") },
            placeholder = { Text("One commitment per line, using Party A and Party B.") },
            maxLines = 6,
        )
        Button(
            onClick = {
                summary = summaryAgent.process(
                    AgreementSummaryRequest(
                        issue = issue,
                        commitments = commitments.lines(),
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Generate Neutral Summary")
        }
        summary?.let { AgreementSummaryPanel(it) }
    }
}

@Composable
private fun AgreementSummaryPanel(summary: AgreementSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Pending Sync", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            Text(summary.summary, fontWeight = FontWeight.SemiBold)
            summary.actions.forEach { Text("- $it") }
            Text(summary.followUp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AgreementScreenPreview() {
    JiraniTheme {
        AgreementScreen()
    }
}
