package com.jirani.app.ui.sync

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.data.local.ReceivedReportItem
import com.jirani.app.data.local.SubmittedReportStatus
import com.jirani.app.data.local.SyncTransport
import com.jirani.app.sync.NearbyConnectionsScanner
import com.jirani.app.ui.common.QuickExitButton
import com.jirani.app.ui.reporting.ScreenTitle
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun SyncScreen(
    modifier: Modifier = Modifier,
    viewModel: NetworkViewModel = viewModel(),
    onQuickExit: () -> Unit = {},
) {
    val context = LocalContext.current
    val network by viewModel.network.collectAsStateWithLifecycle()
    val scanner = remember {
        NearbyConnectionsScanner(
            context = context,
            onReportPacketReceived = viewModel::receiveNearbyReportPacket,
        )
    }
    val scan by scanner.scan.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants.values.all { it }) {
            scanner.start()
        } else {
            viewModel.updateNearbyScan(
                scan.copy(
                    scanning = false,
                    advertising = false,
                    statusMessage = "Nearby permissions are needed to find Jirani devices.",
                ),
            )
        }
    }

    LaunchedEffect(scan) {
        viewModel.updateNearbyScan(scan)
    }

    LaunchedEffect(network.pendingEnvelopes.size, scan.scanning, scan.advertising) {
        if (scan.scanning || scan.advertising) return@LaunchedEffect

        val missingPermissions = context.missingNearbyPermissions()
        if (missingPermissions.isEmpty()) {
            scanner.start()
        } else if (network.pendingEnvelopes.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    LaunchedEffect(scan.connectedDevices.size, network.pendingEnvelopes.size) {
        if (scan.connectedDevices.isNotEmpty() && network.pendingEnvelopes.isNotEmpty()) {
            viewModel.updateNearbyScan(scan)
            scanner.sendReportPackets(viewModel.shareNextReportPackets())
        }
    }

    DisposableEffect(scanner) {
        onDispose { scanner.stop() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            ScreenTitle(
                title = "Sync",
                subtitle = "Saved reports and nearby delivery.",
                modifier = Modifier.weight(1f),
            )
            QuickExitButton(onClick = onQuickExit)
        }
        NearbySyncPanel(
            peerDetected = network.peerDetected,
            nearbyDevices = scan.devices.size,
            connectedDevices = scan.connectedDevices.size,
            waitingCount = network.pendingEnvelopes.size,
            scanning = scan.scanning,
            statusMessage = scan.statusMessage,
            onScan = {
                if (scan.scanning) {
                    scanner.pauseDiscovery()
                    return@NearbySyncPanel
                }

                val missingPermissions = context.missingNearbyPermissions()
                if (missingPermissions.isEmpty()) {
                    scanner.start()
                } else {
                    permissionLauncher.launch(missingPermissions.toTypedArray())
                }
            },
            onSendNext = {
                scanner.sendReportPackets(viewModel.shareNextReportPackets())
            },
        )
        network.lastTransferMessage?.let {
            SyncStatusCard(
                body = it,
            )
        }
        SubmittedReportList(network.submittedReports)
        if (network.receivedReports.isNotEmpty()) {
            ReceivedReportList(network.receivedReports)
        }
    }
}

@Composable
private fun NearbySyncPanel(
    peerDetected: Boolean,
    nearbyDevices: Int,
    connectedDevices: Int,
    waitingCount: Int,
    scanning: Boolean,
    statusMessage: String,
    onScan: () -> Unit,
    onSendNext: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusDot(active = peerDetected)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (peerDetected) {
                            "Nearby devices available"
                        } else if (scanning) {
                            "Scanning for devices"
                        } else {
                            "Scanning paused"
                        },
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$nearbyDevices found - $connectedDevices connected - $waitingCount waiting",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onScan,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) {
                    Text(if (scanning) "Pause scan" else "Find devices")
                }
                Button(
                    onClick = onSendNext,
                    enabled = connectedDevices > 0 && waitingCount > 0,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                ) {
                    Text("Send next")
                }
            }
        }
    }
}

private fun android.content.Context.missingNearbyPermissions(): List<String> {
    val required = when {
        Build.VERSION.SDK_INT >= 32 -> listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.NEARBY_WIFI_DEVICES,
        )
        Build.VERSION.SDK_INT == 31 -> listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
        )
        Build.VERSION.SDK_INT >= 29 -> listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        else -> listOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    return required.filter { permission ->
        ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
    }
}

@Composable
private fun SubmittedReportList(items: List<SubmittedReportStatus>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Submitted reports", fontWeight = FontWeight.SemiBold)
            if (items.isEmpty()) {
                Text(
                    text = "No saved reports yet.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                items.take(6).forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(item.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Sent to ${item.deliveredCount}/${item.maxDevices} devices",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            if (item.stale) "Stale" else item.status,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceivedReportList(items: List<ReceivedReportItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Receiving device inbox", fontWeight = FontWeight.SemiBold)
            items.take(3).forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(item.reportType, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${item.generalArea} - ${item.timeWindow} - ${item.transport.label()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        item.observedRisk,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusDot(active: Boolean) {
    Surface(
        modifier = Modifier.size(14.dp),
        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        shape = CircleShape,
    ) {}
}

@Composable
private fun SyncStatusCard(
    body: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(body)
        }
    }
}

private fun SyncTransport.label(): String = when (this) {
    SyncTransport.NearbyConnections -> "Nearby Connections"
    SyncTransport.WifiDirect -> "Wi-Fi Direct"
    SyncTransport.AndroidShareSheet -> "Android Sharesheet"
    SyncTransport.QrOrEncryptedFile -> "QR/encrypted file"
}

@Preview(showBackground = true)
@Composable
private fun SyncScreenPreview() {
    JiraniTheme {
        SyncScreen()
    }
}
