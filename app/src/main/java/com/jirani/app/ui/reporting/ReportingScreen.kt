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
import com.jirani.app.domain.agent.SafetyReportGuidance
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun ReportingScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportingViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            title = "Safety",
            subtitle = "Stepper-based anonymous reporting with fuzzy location.",
        )
        Stepper(uiState.step)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ThreatChoice(
                label = "Rustling",
                selected = uiState.threatType == "Rustling",
                iconRes = R.drawable.ic_threat_rustling,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Rustling") },
            )
            ThreatChoice(
                label = "Threat",
                selected = uiState.threatType == "Threat",
                iconRes = R.drawable.ic_threat_alert,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Threat") },
            )
            ThreatChoice(
                label = "Rumor",
                selected = uiState.threatType == "Rumor",
                iconRes = R.drawable.ic_threat_rumor,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.selectThreat("Rumor") },
            )
        }
        FuzzyMapCard()
        OutlinedTextField(
            value = uiState.generalLocation,
            onValueChange = viewModel::updateLocation,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("General area") },
            placeholder = { Text("Example: eastern market path") },
        )
        OutlinedTextField(
            value = uiState.details,
            onValueChange = viewModel::updateDetails,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            label = { Text("Observed risk") },
            placeholder = { Text("No names, phone numbers, or exact addresses.") },
            maxLines = 5,
        )
        Button(
            onClick = {
                viewModel.continueToVerify()
                viewModel.submitLocalReport()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save Anonymous Local Report")
        }
        uiState.guidance?.let { ReportingGuidancePanel(it) }
    }
}

@Composable
private fun Stepper(step: ReportStep) {
    val labels = mapOf(
        ReportStep.Threat to "1. Details",
        ReportStep.Location to "2. Region",
        ReportStep.Verify to "3. Local Verification",
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
                    modifier = Modifier.padding(8.dp),
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
        modifier = modifier.height(92.dp),
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
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
            .height(160.dp),
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
                            .padding(22.dp)
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
            Text("Approximate radius only", fontWeight = FontWeight.SemiBold)
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(guidance.incidentSummary, fontWeight = FontWeight.SemiBold)
            Text("Threat type: ${guidance.threatType}")
            Text("Safe next steps", fontWeight = FontWeight.SemiBold)
            guidance.safeNextSteps.forEach { Text("- $it") }
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
