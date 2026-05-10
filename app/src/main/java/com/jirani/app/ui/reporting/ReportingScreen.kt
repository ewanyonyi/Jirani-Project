package com.jirani.app.ui.reporting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.R
import com.jirani.app.data.local.SyncEnvelope
import com.jirani.app.domain.agent.SafetyReportGuidance
import com.jirani.app.ui.common.QuickExitButton
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun ReportingScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportingViewModel = viewModel(),
    onQuickExit: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                title = "Report",
                subtitle = "Start with what happened, not with mediation.",
                modifier = Modifier.weight(1f),
            )
            QuickExitButton(onClick = onQuickExit)
        }
        Stepper(uiState.step)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThreatChoice(
                label = "Violence",
                selected = uiState.threatType == "Violence",
                iconRes = R.drawable.ic_threat_alert,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Violence") },
            )
            ThreatChoice(
                label = "Rumor",
                selected = uiState.threatType == "Rumor",
                iconRes = R.drawable.ic_threat_rumor,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Rumor") },
            )
            ThreatChoice(
                label = "Livestock",
                selected = uiState.threatType == "Livestock",
                iconRes = R.drawable.ic_threat_rustling,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Livestock") },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThreatChoice(
                label = "Resource",
                selected = uiState.threatType == "Resource",
                iconRes = R.drawable.ic_nav_mediation,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Resource") },
            )
            ThreatChoice(
                label = "Domestic",
                selected = uiState.threatType == "Domestic",
                iconRes = R.drawable.ic_shield_report,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Domestic") },
            )
            ThreatChoice(
                label = "GBV",
                selected = uiState.threatType == "GBV",
                iconRes = R.drawable.ic_lock,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("GBV") },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThreatChoice(
                label = "Other",
                selected = uiState.threatType == "Other",
                iconRes = R.drawable.ic_more_vert,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Other") },
            )
        }
        FuzzyMapCard()
        OutlinedTextField(
            value = uiState.generalLocation,
            onValueChange = viewModel::updateLocation,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Approximate area") },
            placeholder = { Text("Example: road between Tseikuru and Garissa side") },
        )
        OutlinedTextField(
            value = uiState.details,
            onValueChange = viewModel::updateDetails,
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            label = { Text("What happened?") },
            placeholder = { Text("Example: camels entered a farm, or a survivor needs private help. No names.") },
            maxLines = 5,
        )
        VerificationReminder()
        Button(
            onClick = {
                viewModel.continueToVerify()
                viewModel.submitLocalReport()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save for Local Review")
        }
        uiState.guidance?.let { ReportingGuidancePanel(it) }
        uiState.syncEnvelope?.let { ReportMovementPanel(it) }
    }
}

@Composable
private fun Stepper(step: ReportStep) {
    val labels = mapOf(
        ReportStep.Threat to "1. Details",
        ReportStep.Location to "2. Region",
        ReportStep.Verify to "3. Elder Check",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ReportStep.entries.forEach { item ->
            Surface(
                modifier = Modifier.weight(1f),
                color = if (item.ordinal <= step.ordinal) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = labels.getValue(item),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                    color = if (item.ordinal <= step.ordinal) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun ThreatChoice(
    label: String,
    selected: Boolean,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.height(76.dp),
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FuzzyMapCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(0.58f)
                    .alpha(0.22f),
                color = MaterialTheme.colorScheme.error,
                shape = CircleShape,
            ) {}
            listOf(
                0.12f to Alignment.TopStart,
                0.10f to Alignment.TopEnd,
                0.08f to Alignment.BottomStart,
                0.14f to Alignment.BottomEnd,
            ).forEach { (fraction, alignment) ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = alignment,
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(fraction)
                            .alpha(0.18f),
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = CircleShape,
                    ) {}
                }
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize(0.28f)
                    .alpha(0.38f),
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
            ) {}
            Text("Approximate area only", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun VerificationReminder() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Local review comes first", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
            Text("Community conflicts go to trusted local review. Domestic and GBV reports stay private for survivor-chosen support.")
        }
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
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(guidance.incidentSummary, fontWeight = FontWeight.SemiBold)
            Text("Threat type: ${guidance.threatType}")
            Text("Triage: ${guidance.triageOutcome}")
            Text("Mediation: ${guidance.mediationReadiness}")
            Text("Who to involve", fontWeight = FontWeight.SemiBold)
            guidance.localActorsToNotify.forEach { Text("- $it") }
            Text("Next steps", fontWeight = FontWeight.SemiBold)
            guidance.safeNextSteps.forEach { Text("- $it") }
        }
    }
}

@Composable
private fun ReportMovementPanel(envelope: SyncEnvelope) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("How this report can move", fontWeight = FontWeight.SemiBold)
            Text("Audience: ${envelope.audienceTier.label()}")
            Text("State: ${envelope.syncState.label()}")
            Text("Allowed paths: ${envelope.allowedTransports.joinToString { it.label() }}")
            Text("Shared payload keeps only type, general area, time window, risk, and verification status.")
        }
    }
}

private fun com.jirani.app.data.local.SyncAudienceTier.label(): String = when (this) {
    com.jirani.app.data.local.SyncAudienceTier.TrustedVerifier -> "trusted verifier"
    com.jirani.app.data.local.SyncAudienceTier.ProtectionActors -> "protection actors"
    com.jirani.app.data.local.SyncAudienceTier.SurvivorSupportOnly -> "survivor support only"
    com.jirani.app.data.local.SyncAudienceTier.CommunityAlert -> "community alert"
}

private fun com.jirani.app.data.local.SyncState.label(): String = when (this) {
    com.jirani.app.data.local.SyncState.LocalHold -> "held on this phone"
    com.jirani.app.data.local.SyncState.WaitingForTrustedPeer -> "waiting for trusted peer"
    com.jirani.app.data.local.SyncState.ReadyForNearbyShare -> "ready for nearby sharing"
    com.jirani.app.data.local.SyncState.SharedToTrustedPeer -> "shared to trusted peer"
}

private fun com.jirani.app.data.local.SyncTransport.label(): String = when (this) {
    com.jirani.app.data.local.SyncTransport.NearbyConnections -> "Nearby Connections"
    com.jirani.app.data.local.SyncTransport.WifiDirect -> "Wi-Fi Direct"
    com.jirani.app.data.local.SyncTransport.AndroidShareSheet -> "Android Sharesheet"
    com.jirani.app.data.local.SyncTransport.QrOrEncryptedFile -> "QR/encrypted file"
}

@Composable
internal fun ScreenTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
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
