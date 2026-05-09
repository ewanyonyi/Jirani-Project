package com.jirani.app.ui.agreement

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
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
import com.jirani.app.data.local.AgreementRecordStatus
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
            AssistChip(
                onClick = { viewModel.toggleFilter(AgreementRecordStatus.Signed) },
                label = { Text("Signed") },
                leadingIcon = { FilterDot(active = uiState.selectedFilter == AgreementRecordStatus.Signed) },
            )
            AssistChip(
                onClick = { viewModel.toggleFilter(AgreementRecordStatus.Draft) },
                label = { Text("Draft") },
                leadingIcon = { FilterDot(active = uiState.selectedFilter == AgreementRecordStatus.Draft) },
            )
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecordStatusBadge(agreement.recordStatus)
                SyncBadge(agreement.syncStatus)
            }
        }
    }
}

@Composable
private fun FilterDot(active: Boolean) {
    Surface(
        modifier = Modifier.size(8.dp),
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        shape = MaterialTheme.shapes.extraSmall,
    ) {}
}

@Composable
private fun RecordStatusBadge(status: AgreementRecordStatus) {
    val label = when (status) {
        AgreementRecordStatus.Draft -> "Draft"
        AgreementRecordStatus.Signed -> "Signed"
    }
    Surface(
        color = if (status == AgreementRecordStatus.Signed) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
        },
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SyncBadge(syncStatus: SyncStatus) {
    val badge = when (syncStatus) {
        SyncStatus.Local -> SyncBadgeUi(R.drawable.ic_incoming_data, "Local Only", MaterialTheme.colorScheme.tertiary)
        SyncStatus.Mesh -> SyncBadgeUi(R.drawable.ic_mesh_status, "Mesh Synced", MaterialTheme.colorScheme.secondary)
        SyncStatus.Cloud -> SyncBadgeUi(R.drawable.ic_share_data, "Gateway Uploaded", MaterialTheme.colorScheme.primary)
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(badge.iconRes),
                contentDescription = null,
                tint = badge.color,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = badge.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private data class SyncBadgeUi(
    @DrawableRes val iconRes: Int,
    val label: String,
    val color: androidx.compose.ui.graphics.Color,
)

@Preview(showBackground = true)
@Composable
private fun AgreementScreenPreview() {
    JiraniTheme {
        AgreementScreen()
    }
}
