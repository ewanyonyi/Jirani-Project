package com.jirani.app.sync

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
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
import com.jirani.app.data.local.WireReportPacket
import com.jirani.app.data.local.SyncTransport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class NearbyScanSnapshot(
    val scanning: Boolean = false,
    val advertising: Boolean = false,
    val devices: List<NearbyJiraniDevice> = emptyList(),
    val connectedDevices: List<NearbyJiraniDevice> = emptyList(),
    val statusMessage: String = "Nearby scan is off.",
)

class NearbyConnectionsScanner(
    context: Context,
    private val onReportPacketReceived: (WireReportPacket, String) -> Unit = { _, _ -> },
    private val onReportPacketsSent: (List<WireReportPacket>) -> Unit = {},
    private val hasWaitingReports: () -> Boolean = { false },
) {
    private val appContext = context.applicationContext
    private val client = Nearby.getConnectionsClient(appContext)
    private val discovered = linkedMapOf<String, NearbyJiraniDevice>()
    private val connected = linkedMapOf<String, NearbyJiraniDevice>()
    private val pendingConnections = mutableSetOf<String>()
    private val localAlias = "Jirani-${stableDeviceSuffix()}"
    private var advertisingStarted = false
    private var discoveryStarted = false

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
            if (hasWaitingReports() || hasInitiatorPriority(info.endpointName)) {
                publishDevices("Found ${discovered.size} nearby Jirani device(s). Connecting.")
                requestConnection(endpointId)
            } else {
                publishDevices("Found ${discovered.size} nearby Jirani device(s). Waiting for connection.")
            }
        }

        override fun onEndpointLost(endpointId: String) {
            discovered.remove(endpointId)
            connected.remove(endpointId)
            pendingConnections.remove(endpointId)
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
            Log.d(Tag, "Connection initiated by ${info.endpointName} endpoint=$endpointId")
            discovered[endpointId] = NearbyJiraniDevice(
                deviceAlias = info.endpointName.ifBlank { "Nearby Jirani device" },
                trusted = true,
                supportedTransports = listOf(SyncTransport.NearbyConnections),
            )
            client.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            pendingConnections.remove(endpointId)
            val message = when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    discovered[endpointId]?.let { connected[endpointId] = it }
                    "Connected to ${connected.size} nearby Jirani device(s)."
                }
                else -> "Nearby connection failed: ${statusLabel(result.status.statusCode)}."
            }
            Log.d(Tag, "Connection result endpoint=$endpointId status=${statusLabel(result.status.statusCode)}")
            publishDevices(message)
        }

        override fun onDisconnected(endpointId: String) {
            connected.remove(endpointId)
            pendingConnections.remove(endpointId)
            Log.d(Tag, "Disconnected endpoint=$endpointId")
            publishDevices("Nearby Jirani device disconnected.")
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            val bytes = payload.asBytes() ?: return
            val packet = NearbyReportPacketCodec.decode(bytes) ?: return
            val fromAlias = connected[endpointId]?.deviceAlias
                ?: discovered[endpointId]?.deviceAlias
                ?: "Nearby Jirani device"
            Log.d(Tag, "Received payload from endpoint=$endpointId alias=$fromAlias bytes=${bytes.size}")
            onReportPacketReceived(packet, fromAlias)
            _scan.update { it.copy(statusMessage = "An anonymized report was received from $fromAlias.") }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) = Unit
    }

    fun start() {
        startAdvertising()
        startDiscovery()
    }

    fun makeAvailable() {
        startAdvertising()
    }

    fun resumeDiscovery() {
        startDiscovery()
    }

    fun pauseDiscovery() {
        client.stopDiscovery()
        discoveryStarted = false
        _scan.update {
            it.copy(
                scanning = false,
                advertising = advertisingStarted,
                statusMessage = if (connected.isEmpty()) {
                    "Discovery paused. This phone is still available to nearby Jirani devices."
                } else {
                    "Discovery paused. Existing nearby connections are still active."
                },
            )
        }
    }

    private fun startAdvertising() {
        if (advertisingStarted) return
        advertisingStarted = true
        _scan.update {
            it.copy(
                advertising = true,
                statusMessage = "This phone is available to nearby Jirani devices.",
            )
        }

        client.startAdvertising(
            localAlias,
            SERVICE_ID,
            connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(STRATEGY).build(),
        ).addOnFailureListener { error ->
            advertisingStarted = false
            _scan.update {
                it.copy(
                    advertising = false,
                    statusMessage = error.localizedMessage ?: "Could not advertise this device.",
                )
            }
        }
    }

    private fun startDiscovery() {
        if (discoveryStarted) return
        discoveryStarted = true
        _scan.update {
            it.copy(
                scanning = true,
                statusMessage = "Scanning for nearby Jirani devices.",
            )
        }

        client.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(STRATEGY).build(),
        ).addOnFailureListener { error ->
            discoveryStarted = false
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
        client.stopAllEndpoints()
        advertisingStarted = false
        discoveryStarted = false
        discovered.clear()
        connected.clear()
        _scan.value = NearbyScanSnapshot(statusMessage = "Nearby device discovery stopped.")
    }

    fun sendReportPackets(packets: List<WireReportPacket>) {
        if (packets.isEmpty()) {
            _scan.update { it.copy(statusMessage = "No eligible report packet is ready to send.") }
            return
        }
        if (connected.isEmpty()) {
            _scan.update { it.copy(statusMessage = "Connect to a nearby Jirani device before sending.") }
            return
        }

        packets.forEachIndexed { index, packet ->
            val endpointId = connected.keys.elementAt(index % connected.size)
            val bytes = NearbyReportPacketCodec.encode(packet)
            Log.d(Tag, "Sending payload to endpoint=$endpointId bytes=${bytes.size}")
            client.sendPayload(endpointId, Payload.fromBytes(bytes))
                .addOnSuccessListener {
                    onReportPacketsSent(listOf(packet))
                    _scan.update { scan -> scan.copy(statusMessage = "Sent anonymized report packet to nearby device.") }
                }
                .addOnFailureListener { error ->
                    Log.w(Tag, "Payload send failed endpoint=$endpointId", error)
                    _scan.update { scan ->
                        scan.copy(statusMessage = error.localizedMessage ?: "Nearby payload send failed.")
                    }
                }
        }
        _scan.update { it.copy(statusMessage = "Sent ${packets.size} anonymized report packet(s).") }
    }

    private fun requestConnection(endpointId: String) {
        if (endpointId in connected || endpointId in pendingConnections) return
        pendingConnections += endpointId
        Log.d(Tag, "Requesting connection endpoint=$endpointId")
        client.requestConnection(localAlias, endpointId, connectionLifecycleCallback)
            .addOnFailureListener { error ->
                pendingConnections.remove(endpointId)
                Log.w(Tag, "Connection request failed endpoint=$endpointId", error)
                _scan.update {
                    it.copy(statusMessage = error.localizedMessage ?: "Could not connect to nearby Jirani device.")
                }
            }
    }

    private fun hasInitiatorPriority(remoteAlias: String): Boolean =
        localAlias < remoteAlias.ifBlank { "Nearby Jirani device" }

    private fun statusLabel(statusCode: Int): String = when (statusCode) {
        ConnectionsStatusCodes.STATUS_OK -> "STATUS_OK"
        ConnectionsStatusCodes.STATUS_ENDPOINT_IO_ERROR -> "STATUS_ENDPOINT_IO_ERROR"
        ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT -> "STATUS_ALREADY_CONNECTED_TO_ENDPOINT"
        ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> "STATUS_CONNECTION_REJECTED"
        ConnectionsStatusCodes.STATUS_ERROR -> "STATUS_ERROR"
        ConnectionsStatusCodes.STATUS_NETWORK_NOT_CONNECTED -> "STATUS_NETWORK_NOT_CONNECTED"
        ConnectionsStatusCodes.STATUS_NOT_CONNECTED_TO_ENDPOINT -> "STATUS_NOT_CONNECTED_TO_ENDPOINT"
        ConnectionsStatusCodes.STATUS_OUT_OF_ORDER_API_CALL -> "STATUS_OUT_OF_ORDER_API_CALL"
        ConnectionsStatusCodes.STATUS_PAYLOAD_IO_ERROR -> "STATUS_PAYLOAD_IO_ERROR"
        else -> "Nearby status $statusCode"
    }

    private fun publishDevices(message: String) {
        _scan.update {
            it.copy(
                devices = discovered.values.toList(),
                connectedDevices = connected.values.toList(),
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
        private const val Tag = "JiraniNearby"
        private const val SERVICE_ID = "com.jirani.app.nearby"
        private val STRATEGY = Strategy.P2P_CLUSTER
    }
}
