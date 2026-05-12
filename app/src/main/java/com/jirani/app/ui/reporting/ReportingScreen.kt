package com.jirani.app.ui.reporting

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.R
import com.jirani.app.ui.common.QuickExitButton
import com.jirani.app.ui.theme.JiraniTheme

private val ViolenceRed = Color(0xFFB71C1C)
private val GbvOrange = Color(0xFFBF360C)
private val ResourceBlue = Color(0xFF0D47A1)
private val LivestockBrown = Color(0xFF4E342E)
private val CropsGreen = Color(0xFF33691E)
private val DomesticRose = Color(0xFF880E4F)
private val OtherGray = Color(0xFF263238)
private val TrustGreen = Color(0xFF0F5A3D)

@Composable
fun ReportingScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportingViewModel = viewModel(),
    onQuickExit: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val speechAvailable = remember(context) { SpeechRecognizer.isRecognitionAvailable(context) }
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            if (spokenText.isNotBlank()) {
                viewModel.updateDetails(appendVoiceText(uiState.details, spokenText))
            }
        }
    }
    val launchSpeechInput = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Briefly describe what happened")
        }
        speechLauncher.launch(intent)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ReportHeader(onQuickExit = onQuickExit)
            TrustIndicators()
            EmergencyShortcut(
                submitting = uiState.submitting,
                onEmergencySos = viewModel::submitEmergencySos,
            )
            ReportStepper(currentStep = uiState.step)

            when (uiState.step) {
                ReportStep.Threat -> DetailsStep(
                    uiState = uiState,
                    onSelectThreat = viewModel::selectThreat,
                    onDetailsChange = viewModel::updateDetails,
                    onVoiceReport = launchSpeechInput,
                    voiceAvailable = speechAvailable,
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
private fun ReportHeader(
    onQuickExit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScreenTitle(
            title = "Report Incident",
            subtitle = "Anonymous - Safe - Offline-ready",
            modifier = Modifier.weight(1f),
        )
        QuickExitButton(onClick = onQuickExit)
    }
}

@Composable
private fun TrustIndicators() {
    val indicators = listOf(
        "Anonymous",
        "No exact GPS",
        "Works offline",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        indicators.forEach { label ->
            TrustIndicator(label = label, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TrustIndicator(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.heightIn(min = 34.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_status_check),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = TrustGreen,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun EmergencyShortcut(
    submitting: Boolean,
    onEmergencySos: () -> Unit,
) {
    OutlinedButton(
        onClick = onEmergencySos,
        enabled = !submitting,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
                contentDescription = "Emergency SOS. Quickly queue an anonymous emergency report."
            },
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_threat_alert),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = "Emergency SOS",
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ReportStepper(currentStep: ReportStep) {
    val steps = listOf(
        ReportStep.Threat to "1 Details",
        ReportStep.Location to "2 Region",
        ReportStep.Verify to "3 Submit",
    )
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEachIndexed { index, item ->
                val (step, label) = item
                val active = step == currentStep
                val complete = step.ordinal < currentStep.ordinal
                StepPill(
                    label = label,
                    active = active,
                    complete = complete,
                    modifier = Modifier.weight(1f),
                )
                if (index < steps.lastIndex) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .weight(0.18f)
                            .height(2.dp)
                            .background(
                                if (complete) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepPill(
    label: String,
    active: Boolean,
    complete: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = when {
        active -> MaterialTheme.colorScheme.primary
        complete -> TrustGreen
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val contentColor = when {
        active -> MaterialTheme.colorScheme.onPrimary
        complete -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier.heightIn(min = 36.dp),
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun DetailsStep(
    uiState: ReportingUiState,
    onSelectThreat: (String) -> Unit,
    onDetailsChange: (String) -> Unit,
    onVoiceReport: () -> Unit,
    voiceAvailable: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("What kind of incident?")
        CategoryGroups(
            selected = uiState.threatType,
            onSelect = onSelectThreat,
        )
        VoiceReportCta(
            enabled = voiceAvailable,
            onClick = onVoiceReport,
        )
        IncidentDetailsField(
            details = uiState.details,
            onDetailsChange = onDetailsChange,
        )
    }
}

@Composable
private fun VoiceReportCta(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .semantics {
                role = Role.Button
                contentDescription = "Tap to speak instead. Voice reporting shortcut."
            },
        onClick = onClick,
        enabled = enabled,
        color = if (enabled) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = if (enabled) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.36f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = if (enabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                contentColor = if (enabled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mic),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = if (enabled) "Tap to speak instead" else "Voice input unavailable",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (enabled) "Useful when typing is hard or unsafe." else "Typing still works offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun IncidentDetailsField(
    details: String,
    onDetailsChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = details,
        onValueChange = onDetailsChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 144.dp)
            .semantics {
                contentDescription = "Incident details. Briefly describe what happened. Do not include names."
            },
        label = { Text("Incident details") },
        placeholder = { Text("Briefly describe what happened. Do not include names.") },
        textStyle = MaterialTheme.typography.bodyLarge,
        minLines = 4,
        maxLines = 7,
        shape = MaterialTheme.shapes.medium,
        colors = reportTextFieldColors(),
    )
}

private fun appendVoiceText(
    current: String,
    spokenText: String,
): String = listOf(current.trim(), spokenText.trim())
    .filter { it.isNotBlank() }
    .joinToString(separator = "\n")

@Composable
private fun RegionStep(
    area: String,
    onAreaChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionLabel("Approximate region")
        Text(
            text = "Use a road, village, market, or landmark. Exact homes and GPS are not shared.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
        OutlinedTextField(
            value = area,
            onValueChange = onAreaChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp)
                .semantics {
                    contentDescription = "Approximate area. Do not enter exact home location."
                },
            label = { Text("Approximate area") },
            placeholder = { Text("Example: Tseikuru-Garissa road area") },
            textStyle = MaterialTheme.typography.bodyLarge,
            minLines = 2,
            colors = reportTextFieldColors(),
            shape = MaterialTheme.shapes.medium,
        )
    }
}

@Composable
private fun SendStep(uiState: ReportingUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Ready to submit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            SummaryRow("Category", uiState.threatType.ifBlank { "Not selected" })
            SummaryRow("Region", uiState.generalLocation.ifBlank { "Not specified" })
            Text(
                text = "The report stays anonymous and can wait offline until a trusted device is available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun CategoryGroups(
    selected: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        reportCategoryGroups().forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (group.critical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                CategoryGrid(
                    categories = group.categories,
                    selected = selected,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<ReportCategory>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    BoxWithConstraints {
        val columns = if (maxWidth < 340.dp) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categories.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItems.forEach { category ->
                        CategoryTile(
                            category = category,
                            selected = selected == category.label,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelect(category.label) },
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
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
    val haptic = LocalHapticFeedback.current
    val selectedScale by animateFloatAsState(
        targetValue = if (selected) 1.01f else 1f,
        label = "category-selected-scale",
    )
    val tileColor = if (selected) {
        category.color.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    Surface(
        modifier = modifier
            .scale(selectedScale)
            .heightIn(min = if (category.critical) 86.dp else 76.dp)
            .defaultMinSize(minHeight = 48.dp)
            .semantics {
                role = Role.Button
                this.selected = selected
                contentDescription = "${category.label} incident category${if (selected) ", selected" else ""}"
            },
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        color = tileColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (selected) 1.dp else 0.dp,
        shadowElevation = if (selected) 1.dp else 0.dp,
        border = BorderStroke(
            width = if (selected) 2.5.dp else 1.dp,
            color = if (selected) category.color else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                color = category.color.copy(alpha = if (category.critical) 0.16f else 0.10f),
                contentColor = category.color,
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(
                    painter = painterResource(category.iconRes),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Text(
                text = category.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_status_check),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = category.color,
                )
            }
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
        ReportStep.Verify -> if (uiState.submitting) "Submitting..." else "Submit Report"
        else -> "Continue"
    }
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.995f,
        label = "report-cta-scale",
    )
    Button(
        onClick = {
            if (uiState.step == ReportStep.Verify) onSubmit() else onNext()
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .defaultMinSize(minHeight = 56.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 1.dp,
            disabledElevation = 0.dp,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SubmissionReceiptPanel(
    message: String,
    submitting: Boolean,
) {
    val scale by animateFloatAsState(
        targetValue = if (submitting) 0.99f else 1f,
        label = "receipt-scale",
    )
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.44f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Report queued",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun reportTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.94f),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.94f),
)

private data class ReportCategoryGroup(
    val label: String,
    val critical: Boolean,
    val categories: List<ReportCategory>,
)

private data class ReportCategory(
    val label: String,
    @DrawableRes val iconRes: Int,
    val color: Color,
    val critical: Boolean = false,
)

private fun reportCategoryGroups(): List<ReportCategoryGroup> = listOf(
    ReportCategoryGroup(
        label = "Critical",
        critical = true,
        categories = listOf(
            ReportCategory("Violence", R.drawable.ic_threat_alert, ViolenceRed, critical = true),
            ReportCategory("GBV", R.drawable.ic_lock, GbvOrange, critical = true),
        ),
    ),
    ReportCategoryGroup(
        label = "Community",
        critical = false,
        categories = listOf(
            ReportCategory("Resource", R.drawable.ic_nav_mediation, ResourceBlue),
            ReportCategory("Domestic", R.drawable.ic_shield_report, DomesticRose),
        ),
    ),
    ReportCategoryGroup(
        label = "Agricultural",
        critical = false,
        categories = listOf(
            ReportCategory("Livestock", R.drawable.ic_threat_rustling, LivestockBrown),
            ReportCategory("Crops", R.drawable.ic_threat_crops, CropsGreen),
        ),
    ),
    ReportCategoryGroup(
        label = "Misc",
        critical = false,
        categories = listOf(
            ReportCategory("Other", R.drawable.ic_more_vert, OtherGray),
        ),
    ),
)

@Composable
internal fun ScreenTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
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
