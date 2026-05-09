package com.jirani.app.ui.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jirani.app.ui.reporting.ScreenTitle
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun SyncScreen(
    onOpenTranslation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            title = "Local Sync",
            subtitle = "Track records prepared for delayed peer exchange and gateway handoff.",
        )
        SyncStatusCard(
            title = "Storage",
            body = "Local records are planned for Room/SQLite with encrypted sensitive fields.",
        )
        SyncStatusCard(
            title = "Ghost-Sync",
            body = "Peer exchange placeholders are ready for BLE, Wi-Fi Direct, Nearby Connections, or Android sharing.",
        )
        SyncStatusCard(
            title = "Pending queue",
            body = "No sync envelopes yet. This screen will show delayed sharing history after local records are added.",
        )
        Button(
            onClick = onOpenTranslation,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Open Translation")
        }
    }
}

@Composable
private fun SyncStatusCard(
    title: String,
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SyncScreenPreview() {
    JiraniTheme {
        SyncScreen(onOpenTranslation = {})
    }
}
