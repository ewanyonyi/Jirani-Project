package com.jirani.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.jirani.app.R
import com.jirani.app.ui.agreement.AgreementScreen
import com.jirani.app.ui.mediation.MediationScreen
import com.jirani.app.ui.reporting.ReportingScreen
import com.jirani.app.ui.sync.SyncScreen
import com.jirani.app.ui.theme.JiraniTheme

private enum class JiraniDestination(
    val title: String,
    val navLabel: String,
    val marker: String,
) {
    Mediation("Mediation", "Mediation", "M"),
    Vault("Vault", "Vault", "V"),
    Safety("Safety", "Safety", "S"),
    Network("Network", "Network", "N"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JiraniApp() {
    var selectedDestination by rememberSaveable { mutableStateOf(JiraniDestination.Mediation) }
    var darkTheme by rememberSaveable { mutableStateOf(false) }

    JiraniTheme(darkTheme = darkTheme) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(selectedDestination.title)
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    ),
                    navigationIcon = {
                        IconButton(
                            onClick = { darkTheme = !darkTheme },
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (darkTheme) R.drawable.ic_light_mode else R.drawable.ic_dark_mode,
                                ),
                                contentDescription = if (darkTheme) "Switch to light mode" else "Switch to dark mode",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { selectedDestination = JiraniDestination.Network }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share_data),
                                contentDescription = "Outgoing data sharing",
                            )
                        }
                        IconButton(onClick = { selectedDestination = JiraniDestination.Vault }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_incoming_data),
                                contentDescription = "Incoming data",
                            )
                        }
                        IconButton(onClick = { selectedDestination = JiraniDestination.Network }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_mesh_status),
                                contentDescription = "Network status",
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { selectedDestination = JiraniDestination.Safety }) {
                    Text("!")
                }
            },
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
                JiraniDestination.Mediation -> MediationScreen(modifier = modifier)
                JiraniDestination.Vault -> AgreementScreen(modifier = modifier)
                JiraniDestination.Safety -> ReportingScreen(modifier = modifier)
                JiraniDestination.Network -> SyncScreen(modifier = modifier)
            }
        }
    }
}
