package com.jirani.app.ui.reporting

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.R
import com.jirani.app.ui.common.QuickExitButton
import com.jirani.app.ui.theme.JiraniTheme

private val PaperBackground = Color(0xFFF9F7F2)
private val BrandGreen = Color(0xFF1B5E20)
private val ViolenceRed = Color(0xFFC62828)
private val GbvOrange = Color(0xFFE65100)
private val ResourceBlue = Color(0xFF1565C0)
private val RumorPurple = Color(0xFF6A1B9A)
private val LivestockBrown = Color(0xFF6D4C41)
private val DomesticRose = Color(0xFFAD1457)
private val OtherGray = Color(0xFF455A64)

@Composable
fun ReportingScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportingViewModel = viewModel(),
    onQuickExit: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PaperBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                ScreenTitle(
                    title = "Report Incident",
                    subtitle = "Capture the risk without names or exact homes.",
                    modifier = Modifier.weight(1f),
                )
                QuickExitButton(onClick = onQuickExit)
            }

            ReportProgressBar(currentStep = uiState.step)

            when (uiState.step) {
                ReportStep.Threat -> DetailsStep(
                    uiState = uiState,
                    onSelectThreat = viewModel::selectThreat,
                    onDetailsChange = viewModel::updateDetails,
                )
                ReportStep.Location -> RegionStep(
                    area = uiState.generalLocation,
                    onAreaChange = viewModel::updateLocation,
                )
                ReportStep.Verify -> SendStep(uiState = uiState)
            }

            FooterButton(
                uiState = uiState,
                onNext = viewModel::nextStep,
                onSubmit = viewModel::submitLocalReport,
            )

            AnimatedVisibility(
                visible = uiState.submissionMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                uiState.submissionMessage?.let {
                    SubmissionReceiptPanel(
                        message = it,
                        submitting = uiState.submitting,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportProgressBar(currentStep: ReportStep) {
    val steps = listOf(
        ReportStep.Threat to "Details",
        ReportStep.Location to "Region",
        ReportStep.Verify to "Send",
    )
    val progress by animateFloatAsState(
        targetValue = (currentStep.ordinal + 1) / steps.size.toFloat(),
        label = "report-progress",
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 8.dp),
            color = BrandGreen,
            trackColor = MaterialTheme.colorScheme.surface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            steps.forEach { (step, label) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (step == currentStep) FontWeight.Bold else FontWeight.Medium,
                    color = if (step.ordinal <= currentStep.ordinal) BrandGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DetailsStep(
    uiState: ReportingUiState,
    onSelectThreat: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CategoryGrid(
            selected = uiState.threatType,
            onSelect = onSelectThreat,
        )
        OutlinedTextField(
            value = uiState.details,
            onValueChange = onDetailsChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 128.dp),
            label = { Text("What happened?") },
            placeholder = { Text("Example: camels entered a farm near the road. No names.") },
            trailingIcon = {
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(R.drawable.ic_mic),
                        contentDescription = "Record voice note",
                        tint = BrandGreen,
                    )
                }
            },
            maxLines = 6,
            colors = reportTextFieldColors(),
        )
    }
}

@Composable
private fun RegionStep(
    area: String,
    onAreaChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = area,
        onValueChange = onAreaChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Approximate area") },
        placeholder = { Text("Example: Tseikuru-Garissa road area") },
        singleLine = false,
        colors = reportTextFieldColors(),
    )
}

@Composable
private fun SendStep(uiState: ReportingUiState) {
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
            Text("Ready to send", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Category: ${uiState.threatType}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Area: ${uiState.generalLocation.ifBlank { "not specified" }}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "The report will be anonymized before it moves to trusted nearby devices.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    selected: String,
    onSelect: (String) -> Unit,
) {
    val categories = reportCategories()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        categories.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowItems.forEach { category ->
                    CategoryTile(
                        category = category,
                        selected = selected == category.label,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(category.label) },
                    )
                }
                repeat(3 - rowItems.size) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(
    category: ReportCategory,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val tileColor = if (selected) category.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
    Surface(
        modifier = modifier
            .heightIn(min = 88.dp)
            .defaultMinSize(minHeight = 48.dp),
        onClick = onClick,
        color = tileColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) category.color else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(category.iconRes),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = category.color,
            )
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FooterButton(
    uiState: ReportingUiState,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
) {
    val enabled = when (uiState.step) {
        ReportStep.Threat -> uiState.detailsValid
        ReportStep.Location -> true
        ReportStep.Verify -> uiState.detailsValid && !uiState.submitting
    }
    val text = when (uiState.step) {
        ReportStep.Verify -> if (uiState.submitting) "Submitting..." else "Send Report"
        else -> "Next"
    }
    Button(
        onClick = {
            if (uiState.step == ReportStep.Verify) onSubmit() else onNext()
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandGreen,
            contentColor = Color.White,
            disabledContainerColor = BrandGreen.copy(alpha = 0.28f),
            disabledContentColor = Color.White.copy(alpha = 0.72f),
        ),
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SubmissionReceiptPanel(
    message: String,
    submitting: Boolean,
) {
    val scale by animateFloatAsState(
        targetValue = if (submitting) 0.98f else 1f,
        label = "receipt-scale",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        color = BrandGreen.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Report submitted", fontWeight = FontWeight.SemiBold, color = BrandGreen)
            Text(message)
        }
    }
}

@Composable
private fun reportTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = BrandGreen,
    focusedLabelColor = BrandGreen,
    cursorColor = BrandGreen,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
)

private data class ReportCategory(
    val label: String,
    @DrawableRes val iconRes: Int,
    val color: Color,
)

private fun reportCategories(): List<ReportCategory> = listOf(
    ReportCategory("Violence", R.drawable.ic_threat_alert, ViolenceRed),
    ReportCategory("GBV", R.drawable.ic_lock, GbvOrange),
    ReportCategory("Resource", R.drawable.ic_nav_mediation, ResourceBlue),
    ReportCategory("Rumor", R.drawable.ic_threat_rumor, RumorPurple),
    ReportCategory("Livestock", R.drawable.ic_threat_rustling, LivestockBrown),
    ReportCategory("Domestic", R.drawable.ic_shield_report, DomesticRose),
    ReportCategory("Other", R.drawable.ic_more_vert, OtherGray),
)

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
