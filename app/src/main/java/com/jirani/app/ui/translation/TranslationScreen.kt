package com.jirani.app.ui.translation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jirani.app.domain.agent.JiraniLanguage
import com.jirani.app.domain.agent.TranslationAgent
import com.jirani.app.domain.agent.TranslationRequest
import com.jirani.app.domain.agent.TranslationResult
import com.jirani.app.ui.reporting.ScreenTitle
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun TranslationScreen(
    modifier: Modifier = Modifier,
    translationAgent: TranslationAgent = remember { TranslationAgent() },
) {
    var text by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<TranslationResult?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ScreenTitle(
            title = "Translation",
            subtitle = "Prepare simple Swahili phrasing for mediation and safety coordination.",
        )
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            label = { Text("Message") },
            placeholder = { Text("Example: We need to discuss water access peacefully.") },
            maxLines = 6,
        )
        Button(
            onClick = {
                result = translationAgent.process(
                    TranslationRequest(text, JiraniLanguage.Swahili),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Translate to Swahili")
        }
        result?.let { TranslationPanel(it) }
    }
}

@Composable
private fun TranslationPanel(result: TranslationResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(result.translatedText, fontWeight = FontWeight.SemiBold)
            Text(
                text = result.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TranslationScreenPreview() {
    JiraniTheme {
        TranslationScreen()
    }
}
