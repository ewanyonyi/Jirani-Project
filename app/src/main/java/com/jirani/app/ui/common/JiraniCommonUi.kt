package com.jirani.app.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jirani.app.R

@Composable
fun OfflineStatusBanner(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int = R.drawable.ic_status_check,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
fun QuickExitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 44.dp),
    ) {
        Text("Quick Exit", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun CalmModeCard(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "Take a moment. Let's work through this safely and calmly.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "The app keeps names and exact locations out by default.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun ChoiceCard(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp),
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            supportingText?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
