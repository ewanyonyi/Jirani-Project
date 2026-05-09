package com.jirani.app.ui.decoy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jirani.app.ui.theme.JiraniTheme

@Composable
fun DecoyScreen(
    modifier: Modifier = Modifier,
    unlockCode: String = "2468=",
    onUnlock: () -> Unit = {},
) {
    var display by rememberSaveable { mutableStateOf("0") }
    var unlockBuffer by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Bottom),
    ) {
        Text(
            text = "Calculator",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = display,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineLarge,
        )
        listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("C", "0", "=", "+"),
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { key ->
                    Button(
                        onClick = {
                            val nextBuffer = when (key) {
                                "C" -> ""
                                else -> (unlockBuffer + key).takeLast(5)
                            }
                            unlockBuffer = nextBuffer
                            if (nextBuffer == unlockCode) {
                                display = "0"
                                unlockBuffer = ""
                                onUnlock()
                                return@Button
                            }

                            display = when (key) {
                                "C" -> "0"
                                "=" -> display
                                else -> if (display == "0") key else display + key
                            }
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(key)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DecoyScreenPreview() {
    JiraniTheme {
        DecoyScreen()
    }
}
