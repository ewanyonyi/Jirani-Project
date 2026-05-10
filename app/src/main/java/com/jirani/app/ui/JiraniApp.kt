package com.jirani.app.ui

import android.app.Activity
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.rememberDrawerState
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
import kotlinx.coroutines.launch

private enum class JiraniDestination(
    val route: String,
    val title: String,
    val navLabel: String,
    val iconRes: Int,
) {
    Safety("safety", "Report", "Report", R.drawable.ic_nav_safety),
    Mediation("mediation", "Mediation", "Mediate", R.drawable.ic_nav_mediation),
    Vault("vault", "Agreements", "Agree", R.drawable.ic_nav_vault),
    Network("network", "Sync", "Sync", R.drawable.ic_nav_network),
}

private const val DecoyRoute = "decoy"
private const val SettingsRoute = "settings"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JiraniApp() {
    var darkTheme by rememberSaveable { mutableStateOf(false) }

    JiraniTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val activity = LocalContext.current as? Activity
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: JiraniDestination.Safety.route
        val network by LocalFirstUiStore.network.collectAsStateWithLifecycle()
        val securitySettings by LocalFirstUiStore.securitySettings.collectAsStateWithLifecycle()
        val showChrome = currentRoute != DecoyRoute
        val closeApp = {
            activity?.finishAndRemoveTask()
            Unit
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showChrome,
            drawerContent = {
                if (showChrome) {
                    JiraniDrawer(
                        darkTheme = darkTheme,
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            navigateSingleTop(navController, route)
                        },
                        onOpenDecoy = {
                            scope.launch { drawerState.close() }
                            navController.navigate(DecoyRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onToggleTheme = {
                            scope.launch { drawerState.close() }
                            darkTheme = !darkTheme
                        },
                    )
                }
            },
        ) {
            Scaffold(
                topBar = {
                    if (showChrome) {
                        CenterAlignedTopAppBar(
                        title = {
                            Icon(
                                painter = painterResource(R.drawable.ic_jirani_mark),
                                contentDescription = "Jirani",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_menu),
                                    contentDescription = "Open menu",
                                )
                            }
                        },
                            actions = {
                                MeshStatusIcon(
                                    peerDetected = network.peerDetected,
                                    onClick = { navigateSingleTop(navController, JiraniDestination.Network.route) },
                                )
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
                        )
                    }
                },
                bottomBar = {
                    if (showChrome) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp,
                        ) {
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
                                    label = {
                                        Text(
                                            text = destination.navLabel,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip,
                                            softWrap = false,
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    },
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = JiraniDestination.Safety.route,
                    modifier = Modifier.padding(innerPadding),
                ) {
                    composable(JiraniDestination.Mediation.route) { MediationScreen(onQuickExit = closeApp) }
                    composable(JiraniDestination.Vault.route) { AgreementScreen(onQuickExit = closeApp) }
                    composable(JiraniDestination.Safety.route) { ReportingScreen(onQuickExit = closeApp) }
                    composable(JiraniDestination.Network.route) { SyncScreen(onQuickExit = closeApp) }
                    composable(SettingsRoute) { SettingsScreen() }
                    composable(DecoyRoute) {
                        DecoyScreen(
                            unlockCode = securitySettings.discreetCode,
                            onUnlock = {
                                navController.navigate(JiraniDestination.Safety.route) {
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
}

@Composable
private fun JiraniDrawer(
    darkTheme: Boolean,
    onNavigate: (String) -> Unit,
    onOpenDecoy: () -> Unit,
    onToggleTheme: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.width(328.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            DrawerHeader()
            HorizontalDivider()
            DrawerSection("Privacy")
            DrawerItem(R.drawable.ic_calculator, "Disguise Calculator", "Hide the screen quickly", onClick = onOpenDecoy)
            DrawerItem(R.drawable.ic_settings, "Settings", "Discreet code and preferences") {
                onNavigate(SettingsRoute)
            }
            DrawerItem(
                iconRes = if (darkTheme) R.drawable.ic_light_mode else R.drawable.ic_dark_mode,
                title = if (darkTheme) "Light Mode" else "Dark Mode",
                subtitle = "Change display style",
                onClick = onToggleTheme,
            )
            DrawerSection("Saved Work")
            DrawerItem(R.drawable.ic_shield_report, "Saved Reports", "Delivery count by report") {
                onNavigate(JiraniDestination.Network.route)
            }
        }
    }
}

@Composable
private fun DrawerHeader() {
    Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 30.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(58.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_jirani_mark),
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = "Jirani",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DrawerSection(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun DrawerItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
