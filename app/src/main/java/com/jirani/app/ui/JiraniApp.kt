package com.jirani.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jirani.app.ui.agreement.AgreementScreen
import com.jirani.app.ui.home.HomeScreen
import com.jirani.app.ui.mediation.MediationScreen
import com.jirani.app.ui.reporting.ReportingScreen
import com.jirani.app.ui.sync.SyncScreen
import com.jirani.app.ui.translation.TranslationScreen

private enum class JiraniDestination(
    val title: String,
    val navLabel: String,
    val marker: String,
) {
    Home("Home", "Home", "H"),
    Mediation("Mediation", "Mediate", "M"),
    Reporting("Reporting", "Report", "R"),
    Agreements("Agreements", "Agree", "A"),
    Translation("Translation", "Translate", "T"),
    Sync("Sync", "Sync", "S"),
}

@Composable
fun JiraniApp() {
    var selectedDestination by rememberSaveable { mutableStateOf(JiraniDestination.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                JiraniDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = { selectedDestination = destination },
                        icon = { Text(destination.marker) },
                        label = { Text(destination.navLabel) },
                    )
                }
            }
        },
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (selectedDestination) {
            JiraniDestination.Home -> HomeScreen(
                modifier = modifier,
                onOpenMediation = { selectedDestination = JiraniDestination.Mediation },
                onOpenReporting = { selectedDestination = JiraniDestination.Reporting },
                onOpenAgreements = { selectedDestination = JiraniDestination.Agreements },
                onOpenSync = { selectedDestination = JiraniDestination.Sync },
            )

            JiraniDestination.Mediation -> MediationScreen(modifier = modifier)
            JiraniDestination.Reporting -> ReportingScreen(modifier = modifier)
            JiraniDestination.Agreements -> AgreementScreen(modifier = modifier)
            JiraniDestination.Translation -> TranslationScreen(modifier = modifier)
            JiraniDestination.Sync -> SyncScreen(
                modifier = modifier,
                onOpenTranslation = { selectedDestination = JiraniDestination.Translation },
            )
        }
    }
}
