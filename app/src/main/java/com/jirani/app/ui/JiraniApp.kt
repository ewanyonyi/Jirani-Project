package com.jirani.app.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jirani.app.R
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.ui.agreement.AgreementScreen
import com.jirani.app.ui.decoy.DecoyScreen
import com.jirani.app.ui.mediation.MediationScreen
import com.jirani.app.ui.reporting.ReportingScreen
import com.jirani.app.ui.settings.SettingsScreen
import com.jirani.app.ui.sync.SyncScreen
import com.jirani.app.ui.theme.JiraniTheme

private enum class JiraniDestination(
    val route: String,
    val title: String,
    val navLabel: String,
    val iconRes: Int,
) {
    Mediation("mediation", "Mediation", "Mediation", R.drawable.ic_nav_mediation),
    Vault("vault", "Vault", "Vault", R.drawable.ic_nav_vault),
    Safety("safety", "Safety", "Safety", R.drawable.ic_nav_safety),
    Network("network", "Network", "Network", R.drawable.ic_nav_network),
}

private const val DecoyRoute = "decoy"
private const val SettingsRoute = "settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JiraniApp() {
    var darkTheme by rememberSaveable { mutableStateOf(false) }
    var overflowMenuExpanded by rememberSaveable { mutableStateOf(false) }

    JiraniTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: JiraniDestination.Mediation.route
        val network by LocalFirstUiStore.network.collectAsStateWithLifecycle()
        val securitySettings by LocalFirstUiStore.securitySettings.collectAsStateWithLifecycle()
        val showChrome = currentRoute != DecoyRoute

        Scaffold(
            topBar = {
                if (showChrome) {
                    CenterAlignedTopAppBar(
                        title = {
                            Icon(
                                painter = painterResource(R.drawable.ic_jirani_mark),
                                contentDescription = "Jirani",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    navController.navigate(DecoyRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                },
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_calculator),
                                    contentDescription = "Open decoy",
                                )
                            }
                        },
                        actions = {
                            MeshStatusIcon(
                                peerDetected = network.peerDetected,
                                onClick = { navigateSingleTop(navController, JiraniDestination.Network.route) },
                            )
                            IconButton(onClick = { overflowMenuExpanded = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_more_vert),
                                    contentDescription = "More options",
                                )
                            }
                            DropdownMenu(
                                expanded = overflowMenuExpanded,
                                onDismissRequest = { overflowMenuExpanded = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_settings),
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        overflowMenuExpanded = false
                                        navigateSingleTop(navController, SettingsRoute)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(if (darkTheme) "Light mode" else "Dark mode") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(
                                                if (darkTheme) R.drawable.ic_light_mode else R.drawable.ic_dark_mode,
                                            ),
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        overflowMenuExpanded = false
                                        darkTheme = !darkTheme
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Outgoing data") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_share_data),
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        overflowMenuExpanded = false
                                        navigateSingleTop(navController, JiraniDestination.Network.route)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Incoming data") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_incoming_data),
                                            contentDescription = null,
                                        )
                                    },
                                    onClick = {
                                        overflowMenuExpanded = false
                                        navigateSingleTop(navController, JiraniDestination.Vault.route)
                                    },
                                )
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                if (showChrome) {
                    ExtendedFloatingActionButton(
                        onClick = { navigateSingleTop(navController, JiraniDestination.Safety.route) },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_shield_report),
                                contentDescription = null,
                            )
                        },
                        text = { Text("Report") },
                    )
                }
            },
            bottomBar = {
                if (showChrome) {
                    NavigationBar {
                        JiraniDestination.entries.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = { navigateSingleTop(navController, destination.route) },
                                icon = {
                                    Icon(
                                        painter = painterResource(destination.iconRes),
                                        contentDescription = destination.title,
                                    )
                                },
                                label = { Text(destination.navLabel) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = JiraniDestination.Mediation.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(JiraniDestination.Mediation.route) { MediationScreen() }
                composable(JiraniDestination.Vault.route) { AgreementScreen() }
                composable(JiraniDestination.Safety.route) { ReportingScreen() }
                composable(JiraniDestination.Network.route) { SyncScreen() }
                composable(SettingsRoute) { SettingsScreen() }
                composable(DecoyRoute) {
                    DecoyScreen(
                        unlockCode = securitySettings.discreetCode,
                        onUnlock = {
                            navController.navigate(JiraniDestination.Mediation.route) {
                                popUpTo(DecoyRoute) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MeshStatusIcon(
    peerDetected: Boolean,
    onClick: () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "mesh-pulse")
    val alpha by transition.animateFloat(
        initialValue = 0.48f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mesh-alpha",
    )

    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.ic_mesh_status),
            contentDescription = if (peerDetected) "Peer detected" else "No peer detected",
            tint = if (peerDetected) Color(0xFF69F0AE) else Color.LightGray,
            modifier = Modifier.graphicsLayer(alpha = if (peerDetected) alpha else 0.48f),
        )
    }
}

private fun navigateSingleTop(
    navController: androidx.navigation.NavHostController,
    route: String,
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
