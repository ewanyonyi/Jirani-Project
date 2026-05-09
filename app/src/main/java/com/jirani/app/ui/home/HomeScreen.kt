package com.jirani.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun HomeScreen(
    onOpenMediation: () -> Unit,
    onOpenReporting: () -> Unit,
    onOpenAgreements: () -> Unit,
    onOpenSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppHeader()
        StatusBand()
        PrimaryActions(
            onOpenMediation = onOpenMediation,
            onOpenReporting = onOpenReporting,
        )
        WorkflowGrid(
            onOpenAgreements = onOpenAgreements,
            onOpenSync = onOpenSync,
        )
        PrivacyNote()
    }
}

@Composable
private fun AppHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Jirani",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Offline peace coordination",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Guide mediation, preserve agreements, and verify local safety updates without requiring personal identity.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatusBand() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Local mode active",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Guidance runs on device. Records are prepared for encrypted local storage and delayed peer sync.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PrimaryActions(
    onOpenMediation: () -> Unit,
    onOpenReporting: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onOpenMediation,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start Mediation")
        }
        OutlinedButton(
            onClick = onOpenReporting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create Anonymous Report")
        }
    }
}

@Composable
private fun WorkflowGrid(
    onOpenAgreements: () -> Unit,
    onOpenSync: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Workflows",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            WorkflowCard(
                title = "Agreements",
                detail = "Draft neutral records with anonymous party labels.",
                modifier = Modifier.weight(1f),
                onClick = onOpenAgreements,
            )
            WorkflowCard(
                title = "Sync",
                detail = "Review local sync status and delayed sharing.",
                modifier = Modifier.weight(1f),
                onClick = onOpenSync,
            )
        }
    }
}

@Composable
private fun WorkflowCard(
    title: String,
    detail: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PrivacyNote() {
    Text(
        text = "No mandatory phone numbers, IDs, or central-server participation.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    JiraniTheme {
        HomeScreen(
            onOpenMediation = {},
            onOpenReporting = {},
            onOpenAgreements = {},
            onOpenSync = {},
        )
    }
}
