package com.jirani.app.ui.sync

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.ui.reporting.ScreenTitle
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun SyncScreen(
    modifier: Modifier = Modifier,
    viewModel: NetworkViewModel = viewModel(),
) {
    val network by viewModel.network.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            title = "Network",
            subtitle = "BLE/Wi-Fi Direct peer scan and delayed local sync queue.",
        )
        GhostSyncRadar(network.peerDetected)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NetworkStatCard("Nearby Neighbors", network.nearbyNeighbors.toString(), Modifier.weight(1f))
            NetworkStatCard("Queue Size", network.queueSize.toString(), Modifier.weight(1f))
        }
        Button(
            onClick = viewModel::togglePeerSimulation,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (network.peerDetected) "Stop Peer Simulation" else "Simulate Peer Detected")
        }
        SyncStatusCard(
            title = "Local-first queue",
            body = "UI actions save locally first. Mesh or cloud sync happens later as a side effect.",
        )
        SyncStatusCard(
            title = "Ghost-Sync transport",
            body = "Transport adapters remain replaceable for BLE, Wi-Fi Direct, Nearby Connections, or Android sharing.",
        )
    }
}

@Composable
private fun GhostSyncRadar(peerDetected: Boolean) {
    val transition = rememberInfiniteTransition(label = "radar")
    val scale by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "radar-scale",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.medium,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .alpha(0.22f),
                color = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {}
            Surface(
                modifier = Modifier.size(82.dp),
                color = if (peerDetected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (peerDetected) "Mesh" else "Scan",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.labelMedium)
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
        SyncScreen()
    }
}
