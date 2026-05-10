package com.jirani.app.sync

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.jirani.app.data.local.NearbyJiraniDevice
import com.jirani.app.data.local.SyncTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class NearbyScanSnapshot(
    val scanning: Boolean = false,
    val advertising: Boolean = false,
    val devices: List<NearbyJiraniDevice> = emptyList(),
    val statusMessage: String = "Nearby scan is off.",
)

class NearbyConnectionsScanner(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val client = Nearby.getConnectionsClient(appContext)
    private val discovered = linkedMapOf<String, NearbyJiraniDevice>()
    private val localAlias = "Jirani-${stableDeviceSuffix()}"

    private val _scan = MutableStateFlow(NearbyScanSnapshot())
    val scan: StateFlow<NearbyScanSnapshot> = _scan

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            if (info.serviceId != SERVICE_ID) return

            discovered[endpointId] = NearbyJiraniDevice(
                deviceAlias = info.endpointName.ifBlank { "Nearby Jirani device" },
                trusted = true,
                supportedTransports = listOf(SyncTransport.NearbyConnections),
            )
            publishDevices("Found ${discovered.size} nearby Jirani device(s).")
        }

        override fun onEndpointLost(endpointId: String) {
            discovered.remove(endpointId)
            publishDevices(
                if (discovered.isEmpty()) {
                    "No nearby Jirani device is visible right now."
                } else {
                    "Found ${discovered.size} nearby Jirani device(s)."
                },
            )
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            val message = when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> "Nearby Jirani device connected."
                else -> "Nearby connection was not completed."
            }
            _scan.update { it.copy(statusMessage = message) }
        }

        override fun onDisconnected(endpointId: String) {
            _scan.update { it.copy(statusMessage = "Nearby Jirani device disconnected.") }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) = Unit

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    fun start() {
        discovered.clear()
        _scan.value = NearbyScanSnapshot(
            scanning = true,
            advertising = true,
            statusMessage = "Scanning for nearby Jirani devices.",
        )

        client.startAdvertising(
            localAlias,
            SERVICE_ID,
            connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(STRATEGY).build(),
        ).addOnFailureListener { error ->
            _scan.update {
                it.copy(
                    advertising = false,
                    statusMessage = error.localizedMessage ?: "Could not advertise this device.",
                )
            }
        }

        client.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(STRATEGY).build(),
        ).addOnFailureListener { error ->
            _scan.update {
                it.copy(
                    scanning = false,
                    statusMessage = error.localizedMessage ?: "Could not scan for nearby devices.",
                )
            }
        }
    }

    fun stop() {
        client.stopDiscovery()
        client.stopAdvertising()
        discovered.clear()
        _scan.value = NearbyScanSnapshot(statusMessage = "Nearby device discovery stopped.")
    }

    private fun publishDevices(message: String) {
        _scan.update {
            it.copy(
                devices = discovered.values.toList(),
                statusMessage = message,
            )
        }
    }

    private fun stableDeviceSuffix(): String {
        val androidId = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
            ?: Build.MODEL
        return androidId.takeLast(4).uppercase()
    }

    companion object {
        private const val SERVICE_ID = "com.jirani.app.nearby"
        private val STRATEGY = Strategy.P2P_CLUSTER
    }
}
