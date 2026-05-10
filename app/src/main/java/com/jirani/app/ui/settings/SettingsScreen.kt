package com.jirani.app.ui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jirani.app.R
import com.jirani.app.data.local.AppLanguage
import com.jirani.app.data.local.AppThemeMode
import com.jirani.app.ui.theme.JiraniTheme

private val PaperBackground = Color(0xFFF9F7F2)
private val CardShape = RoundedCornerShape(8.dp)
private val SegmentShape = RoundedCornerShape(28.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SectionTitle("Appearance")
        SettingsCard(
            iconRes = R.drawable.ic_light_mode,
            title = "Theme",
            subtitle = "Choose your preferred theme",
        ) {
            ThemeSegmentedControl(
                selected = uiState.themeMode,
                onSelected = viewModel::updateThemeMode,
            )
        }

        SectionTitle("Security")
        SettingsCard(
            iconRes = R.drawable.ic_mesh_status,
            title = "Nearby sharing",
            subtitle = "Automatically share queued anonymized reports with nearby Jirani phones.",
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (uiState.nearbySharingEnabled) "Nearby sharing is on" else "Nearby sharing is off",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Switch(
                    checked = uiState.nearbySharingEnabled,
                    onCheckedChange = viewModel::updateNearbySharing,
                )
            }
        }

        SectionTitle("Regional Settings")
        SettingsCard(
            iconRes = R.drawable.ic_language,
            title = "Language",
            subtitle = "App display language",
        ) {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = uiState.language.label,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    label = { Text("Select Language") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_language),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    AppLanguage.entries.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language.label) },
                            onClick = {
                                viewModel.updateLanguage(language)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }

        if (uiState.message.isNotBlank()) {
            Text(
                text = uiState.message,
                modifier = Modifier.padding(bottom = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        } else {
            Spacer(Modifier.padding(bottom = 10.dp))
        }
    }
}

@Composable
fun DecoyPasscodeScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaperBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SectionTitle("Security")
        SettingsCard(
            iconRes = R.drawable.ic_calculator,
            title = "Calculator decoy",
            subtitle = "Set the code that unlocks Jirani from the calculator screen.",
        ) {
            OutlinedTextField(
                value = uiState.code,
                onValueChange = viewModel::updateCode,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New code") },
                placeholder = { Text("2468=") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.confirmCode,
                onValueChange = viewModel::updateConfirmCode,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm code") },
                placeholder = { Text("2468=") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
            )
            Button(
                onClick = viewModel::saveCode,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text("Save Code")
            }
        }

        Text(
            text = uiState.message,
            modifier = Modifier.padding(bottom = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun SettingsCard(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = CardShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun ThemeSegmentedControl(
    selected: AppThemeMode,
    onSelected: (AppThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeOption(AppThemeMode.System, R.drawable.ic_settings),
        ThemeOption(AppThemeMode.Light, R.drawable.ic_light_mode),
        ThemeOption(AppThemeMode.Dark, R.drawable.ic_dark_mode),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = SegmentShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .clip(SegmentShape)
                .heightIn(min = 52.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEachIndexed { index, option ->
                SegmentButton(
                    option = option,
                    selected = selected == option.mode,
                    onClick = { onSelected(option.mode) },
                    modifier = Modifier.weight(1f),
                )
                if (index != options.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .heightIn(min = 52.dp)
                            .background(MaterialTheme.colorScheme.outline),
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentButton(
    option: ThemeOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp),
        color = background,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(
                    painter = painterResource(option.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = option.mode.label,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
            )
        }
    }
}

private data class ThemeOption(
    val mode: AppThemeMode,
    @DrawableRes val iconRes: Int,
)

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    JiraniTheme {
        SettingsScreen()
    }
}
