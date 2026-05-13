package com.jirani.app.sync

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.jirani.app.data.local.LocalFirstUiStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object NearbySyncRuntime {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val emptyScan = MutableStateFlow(NearbyScanSnapshot())
    private var scanner: NearbyConnectionsScanner? = null
    private var appContext: Context? = null
    private var started = false
    private var disabledForSettings = false

    val scan: StateFlow<NearbyScanSnapshot>
        get() = scanner?.scan ?: emptyScan

    fun initialize(context: Context) {
        if (started) return
        appContext = context.applicationContext
        scanner = NearbyConnectionsScanner(
            context = context.applicationContext,
            onReportPacketReceived = LocalFirstUiStore::receiveNearbyReportPacket,
            onReportPacketsSent = LocalFirstUiStore::markNearbyReportPacketsSent,
            onReportPacketsFailed = LocalFirstUiStore::markNearbyReportPacketsFailed,
            onRelayBundleReceived = LocalFirstUiStore::receiveRelayBundle,
            onRelayBundlesSent = LocalFirstUiStore::markNearbyRelayBundlesSent,
            onRelayBundlesFailed = LocalFirstUiStore::markNearbyRelayBundlesFailed,
            hasWaitingReports = {
                LocalFirstUiStore.hasPendingNearbyReports() ||
                    LocalFirstUiStore.network.value.pendingRelayBundles.isNotEmpty()
            },
        )
        started = true
        bindRuntime()
        if (LocalFirstUiStore.securitySettings.value.nearbySharingEnabled) {
            ensureAvailable()
        }
    }

    fun ensureAvailable() {
        val context = appContext ?: return
        if (!LocalFirstUiStore.securitySettings.value.nearbySharingEnabled) {
            disabledForSettings = true
            scanner?.stop()
            return
        }
        disabledForSettings = false
        if (context.missingNearbyPermissions().isNotEmpty()) return

        val currentScanner = requireScanner()
        currentScanner.makeAvailable()
        if (
            LocalFirstUiStore.hasPendingNearbyReports() ||
            LocalFirstUiStore.network.value.pendingRelayBundles.isNotEmpty()
        ) {
            currentScanner.resumeDiscovery()
        }
    }

    fun stop() {
        scanner?.stop()
    }

    fun missingPermissions(context: Context): List<String> =
        context.applicationContext.missingNearbyPermissions()

    private fun bindRuntime() {
        val currentScanner = requireScanner()

        scope.launch {
            LocalFirstUiStore.securitySettings.collect { settings ->
                if (settings.nearbySharingEnabled) {
                    ensureAvailable()
                } else {
                    disabledForSettings = true
                    currentScanner.stop()
                }
            }
        }

        scope.launch {
            currentScanner.scan.collect { scan ->
                LocalFirstUiStore.updateNearbyDevices(
                    devices = scan.connectedDevices,
                    scanning = scan.scanning,
                )
            }
        }

        scope.launch {
            combine(LocalFirstUiStore.network, currentScanner.scan) { network, scan ->
                (LocalFirstUiStore.hasPendingNearbyReports() || network.pendingRelayBundles.isNotEmpty()) to
                    scan.connectedDevices.isNotEmpty()
            }.collect { (hasWaitingReports, hasConnectedDevices) ->
                if (hasWaitingReports && hasConnectedDevices) {
                    currentScanner.sendReportPackets(LocalFirstUiStore.createNearbyReportPacketsForNextReport())
                    currentScanner.sendRelayBundles(LocalFirstUiStore.createNearbyRelayBundlesForNextBundle())
                }
            }
        }

        scope.launch {
            combine(LocalFirstUiStore.network, currentScanner.scan) { network, scan ->
                NetworkScanState(
                    hasWaitingReports = LocalFirstUiStore.hasPendingNearbyReports(),
                    hasWaitingRelayBundles = network.pendingRelayBundles.isNotEmpty(),
                    hasConnectedDevices = scan.connectedDevices.isNotEmpty(),
                    scanning = scan.scanning,
                    advertising = scan.advertising,
                )
            }.collectLatest { state ->
                val context = appContext ?: return@collectLatest
                if (disabledForSettings) return@collectLatest
                if (!LocalFirstUiStore.securitySettings.value.nearbySharingEnabled) {
                    disabledForSettings = true
                    currentScanner.stop()
                    return@collectLatest
                }
                if (context.missingNearbyPermissions().isNotEmpty()) return@collectLatest

                if (!state.advertising) {
                    currentScanner.makeAvailable()
                }
                if (!state.hasWaitingReports && !state.hasWaitingRelayBundles && state.scanning) {
                    currentScanner.pauseDiscovery()
                    return@collectLatest
                }
                if ((state.hasWaitingReports || state.hasWaitingRelayBundles) && !state.hasConnectedDevices) {
                    currentScanner.resumeDiscovery()
                    delay(DiscoveryBurstMillis)
                    if (
                        (LocalFirstUiStore.hasPendingNearbyReports() ||
                            LocalFirstUiStore.network.value.pendingRelayBundles.isNotEmpty()) &&
                        currentScanner.scan.value.connectedDevices.isEmpty()
                    ) {
                        currentScanner.pauseDiscovery()
                        delay(DiscoveryRestMillis)
                    }
                }
            }
        }
    }

    private fun requireScanner(): NearbyConnectionsScanner =
        scanner ?: error("NearbySyncRuntime.initialize(context) must be called before use.")

    private fun Context.missingNearbyPermissions(): List<String> {
        val required = when {
            Build.VERSION.SDK_INT >= 32 -> listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.NEARBY_WIFI_DEVICES,
            )
            Build.VERSION.SDK_INT == 31 -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            )
            Build.VERSION.SDK_INT >= 29 -> listOf(Manifest.permission.ACCESS_FINE_LOCATION)
            else -> listOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        return required.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
    }

    private data class NetworkScanState(
        val hasWaitingReports: Boolean,
        val hasWaitingRelayBundles: Boolean,
        val hasConnectedDevices: Boolean,
        val scanning: Boolean,
        val advertising: Boolean,
    )

    private const val DiscoveryBurstMillis = 45_000L
    private const val DiscoveryRestMillis = 30_000L
}
