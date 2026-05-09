package com.jirani.app.ui.agreement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.R
import com.jirani.app.data.local.AgreementItem
import com.jirani.app.data.local.SyncStatus
import com.jirani.app.ui.reporting.ScreenTitle
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun AgreementScreen(
    modifier: Modifier = Modifier,
    viewModel: AgreementViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenTitle(
            title = "Vault",
            subtitle = "Encrypted local agreement records with sync status.",
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                text = "Local encrypted vault. BIP-39 recovery phrase ready for future auth.",
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        OutlinedTextField(
            value = uiState.search,
            onValueChange = viewModel::updateSearch,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Local-only search") },
            placeholder = { Text("Search saved agreements") },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("Signed") })
            AssistChip(onClick = {}, label = { Text("Draft") })
            AssistChip(onClick = {}, label = { Text("Synced") })
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(uiState.agreements) { agreement ->
                AgreementCard(agreement)
            }
        }
        OutlinedTextField(
            value = uiState.issue,
            onValueChange = viewModel::updateIssue,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Agreement issue") },
            placeholder = { Text("Example: Shared water pump access") },
        )
        OutlinedTextField(
            value = uiState.commitments,
            onValueChange = viewModel::updateCommitments,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            label = { Text("Commitments") },
            placeholder = { Text("One commitment per line. Use Party A and Party B.") },
            maxLines = 4,
        )
        Button(
            onClick = viewModel::generateSummary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Local Draft")
        }
    }
}

@Composable
private fun AgreementCard(agreement: AgreementItem) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(agreement.title, fontWeight = FontWeight.SemiBold)
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = "Encrypted",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(agreement.summary, style = MaterialTheme.typography.bodyMedium)
            SyncBadge(agreement.syncStatus)
        }
    }
}

@Composable
private fun SyncBadge(syncStatus: SyncStatus) {
    val label = when (syncStatus) {
        SyncStatus.Local -> "Local"
        SyncStatus.Mesh -> "Mesh"
        SyncStatus.Cloud -> "Cloud"
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AgreementScreenPreview() {
    JiraniTheme {
        AgreementScreen()
    }
}
