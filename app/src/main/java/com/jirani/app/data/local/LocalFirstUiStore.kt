package com.jirani.app.data.local

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.ContextCompat
import com.jirani.app.sync.RelayForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

enum class SyncStatus {
    Local,
    Mesh,
    Cloud,
}

enum class AgreementRecordStatus {
    Draft,
    Signed,
}

data class AgreementItem(
    val id: String,
    val title: String,
    val summary: String,
    val syncStatus: SyncStatus,
    val recordStatus: AgreementRecordStatus,
    val encrypted: Boolean = true,
)

data class SyncQueueItem(
    val id: String,
    val title: String,
    val status: String,
)

data class SubmittedReportStatus(
    val envelopeId: String,
    val title: String,
    val deliveredCount: Int,
    val maxDevices: Int,
    val stale: Boolean,
    val status: String,
    val remoteGatewayStatus: String = "Waiting for Rust gateway",
)

data class NetworkSnapshot(
    val peerDetected: Boolean = false,
    val nearbyNeighbors: Int = 0,
    val queueSize: Int = 0,
    val queueItems: List<SyncQueueItem> = emptyList(),
    val pendingEnvelopes: List<SyncEnvelope> = emptyList(),
    val remoteGatewayEnvelopes: List<SyncEnvelope> = emptyList(),
    val pendingRelayBundles: List<RelayBundleCarrierItem> = emptyList(),
    val remoteRelayBundles: List<RelayBundle> = emptyList(),
    val trustedNearbyDevices: List<NearbyJiraniDevice> = emptyList(),
    val receivedReports: List<ReceivedReportItem> = emptyList(),
    val receivedRelayBundles: List<RelayBundleInboxItem> = emptyList(),
    val submittedReports: List<SubmittedReportStatus> = emptyList(),
    val lastTransferMessage: String? = null,
)

data class ReportSubmissionReceipt(
    val envelope: SyncEnvelope,
    val message: String,
)

data class SecuritySettings(
    val discreetCode: String = "2468=",
    val nearbySharingEnabled: Boolean = true,
    val activeRelayModeEnabled: Boolean = false,
    val language: AppLanguage = AppLanguage.English,
    val themeMode: AppThemeMode = AppThemeMode.Light,
)

enum class AppLanguage {
    English,
    Swahili,
    Somali,
    Kamba,
}

enum class AppThemeMode {
    System,
    Light,
    Dark,
}

object LocalFirstUiStore {
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var preferences: SharedPreferences? = null
    private var persistenceStarted = false
    private var appContext: Context? = null

    private val _agreements = MutableStateFlow(
        listOf(
            AgreementItem(
                id = "water-access",
                title = "Water Access Draft",
                summary = "Party A and Party B review shared pump access after three days.",
                syncStatus = SyncStatus.Local,
                recordStatus = AgreementRecordStatus.Draft,
            ),
            AgreementItem(
                id = "grazing-route",
                title = "Grazing Route Record",
                summary = "Community members keep a temporary boundary route while facts are verified.",
                syncStatus = SyncStatus.Mesh,
                recordStatus = AgreementRecordStatus.Signed,
            ),
        ),
    )
    val agreements: StateFlow<List<AgreementItem>> = _agreements

    private val _network = MutableStateFlow(
        NetworkSnapshot(
            peerDetected = false,
            nearbyNeighbors = 0,
            queueSize = 0,
            queueItems = emptyList(),
        ),
    )
    val network: StateFlow<NetworkSnapshot> = _network

    private val _securitySettings = MutableStateFlow(SecuritySettings())
    val securitySettings: StateFlow<SecuritySettings> = _securitySettings

    fun initializePersistence(context: Context) {
        if (persistenceStarted) return
        appContext = context.applicationContext
        RelayBundleRoomStore.initialize(context)
        preferences = context.applicationContext.getSharedPreferences(StoreName, Context.MODE_PRIVATE)
        preferences?.getString(NetworkSnapshotKey, null)?.let { encoded ->
            runCatching { decodeNetworkSnapshot(encoded) }.getOrNull()?.let { restored ->
                _network.value = restored
            }
        }
        preferences?.getString(SecuritySettingsKey, null)?.let { encoded ->
            runCatching { decodeSecuritySettings(encoded) }.getOrNull()?.let { restored ->
                _securitySettings.value = restored
            }
        }
        persistenceStarted = true
        if (_securitySettings.value.activeRelayModeEnabled) {
            startRelayForegroundService()
        }
        persistenceScope.launch {
            val relaySnapshot = RelayBundleRoomStore.load()
            _network.update { current ->
                current.copy(
                    pendingRelayBundles = mergeCarrierItems(
                        current.pendingRelayBundles,
                        relaySnapshot.pendingRelayBundles,
                    ),
                    remoteRelayBundles = (current.remoteRelayBundles + relaySnapshot.remoteRelayBundles)
                        .distinctBy { it.bundleHash },
                    receivedRelayBundles = mergeInboxItems(
                        current.receivedRelayBundles,
                        relaySnapshot.receivedRelayBundles,
                    ),
                )
            }
        }
        persistenceScope.launch {
            _network.drop(1).collect { snapshot ->
                preferences?.edit()
                    ?.putString(NetworkSnapshotKey, encodeNetworkSnapshot(snapshot))
                    ?.apply()
                RelayBundleRoomStore.save(snapshot)
            }
        }
        persistenceScope.launch {
            _securitySettings.drop(1).collect { settings ->
                preferences?.edit()
                    ?.putString(SecuritySettingsKey, encodeSecuritySettings(settings))
                    ?.apply()
            }
        }
    }

    fun saveAgreementDraft(title: String, summary: String) {
        _agreements.update { current ->
            listOf(
                AgreementItem(
                    id = "draft-${current.size + 1}",
                    title = title.ifBlank { "Untitled Agreement" },
                    summary = summary,
                    syncStatus = SyncStatus.Local,
                    recordStatus = AgreementRecordStatus.Draft,
                ),
            ) + current
        }
        enqueueSync()
    }

    fun enqueueSync() {
        _network.update {
            val nextItem = SyncQueueItem(
                id = "queue-${it.queueSize + 1}",
                title = "New local record",
                status = "Waiting to share safely",
            )
            it.copy(
                queueSize = it.queueSize + 1,
                queueItems = listOf(nextItem) + it.queueItems,
            )
        }
    }

    fun saveSafetyReport(
        reportType: String,
        generalLocation: String,
        details: String,
    ): SyncEnvelope {
        val record = ReportingSyncPolicy.createRecord(
            reportType = reportType,
            generalLocation = generalLocation,
            details = details,
        )
        val envelope = ReportingSyncPolicy.createEnvelope(record)
        val remoteGatewayEnvelopes = if (RemoteGatewaySyncPolicy.canUploadToRemoteGateway(envelope)) {
            listOf(envelope)
        } else {
            emptyList()
        }
        val relayBundle = runCatching { RelayBundlePolicy.createBundleFromEnvelope(envelope) }
            .getOrNull()
            ?.takeIf { RelayBundlePolicy.validateForRelay(it) == null }
        _network.update {
            val nextItem = SyncQueueItem(
                id = envelope.envelopeId,
                title = "${record.reportType} report",
                status = envelope.queueStatusLabel(),
            )
            it.copy(
                queueSize = it.queueSize + 1,
                queueItems = listOf(nextItem) + it.queueItems,
                pendingEnvelopes = listOf(envelope) + it.pendingEnvelopes,
                remoteGatewayEnvelopes = remoteGatewayEnvelopes + it.remoteGatewayEnvelopes,
                pendingRelayBundles = relayBundle?.let { bundle ->
                    listOf(RelayBundleCarrierItem(bundle)) + it.pendingRelayBundles
                } ?: it.pendingRelayBundles,
                remoteRelayBundles = relayBundle?.let { bundle ->
                    listOf(bundle) + it.remoteRelayBundles
                } ?: it.remoteRelayBundles,
                submittedReports = listOf(envelope.toSubmittedStatus(nextItem.status)) + it.submittedReports,
            )
        }
        return envelope
    }

    fun submitSafetyReport(
        reportType: String,
        generalLocation: String,
        details: String,
    ): ReportSubmissionReceipt {
        val envelope = saveSafetyReport(reportType, generalLocation, details)
        val message = if (_network.value.trustedNearbyDevices.isNotEmpty()) {
            "Report submitted. Nearby delivery is starting."
        } else {
            "Report submitted. No trusted nearby device available yet; scanning will keep it ready to send."
        }
        _network.update { it.copy(lastTransferMessage = message) }
        return ReportSubmissionReceipt(
            envelope = envelope,
            message = message,
        )
    }

    fun togglePeerSimulation() {
        val nextDetected = !_network.value.peerDetected
        _network.update {
            it.copy(
                peerDetected = nextDetected,
                nearbyNeighbors = if (nextDetected) 5 else 0,
                trustedNearbyDevices = if (nextDetected) {
                    trustedDemoDevices()
                } else {
                    emptyList()
                },
                lastTransferMessage = if (nextDetected) {
                    "Trusted Jirani devices found. Waiting reports can be sent anonymously."
                } else {
                    "Nearby device discovery stopped."
                },
            )
        }
        if (nextDetected) {
            shareNextReportToNearbyDevices()
        }
    }

    fun updateNearbyDevices(
        devices: List<NearbyJiraniDevice>,
        scanning: Boolean,
    ) {
        val uniqueDevices = devices.distinctBy { it.deviceAlias }
        _network.update {
            it.copy(
                peerDetected = scanning && uniqueDevices.isNotEmpty(),
                nearbyNeighbors = uniqueDevices.size,
                trustedNearbyDevices = uniqueDevices,
            )
        }
    }

    fun pendingRemoteGatewayEnvelopes(): List<SyncEnvelope> =
        _network.value.remoteGatewayEnvelopes
            .filter { RemoteGatewaySyncPolicy.canUploadToRemoteGateway(it) }

    fun pendingRemoteRelayBundles(): List<RelayBundle> =
        _network.value.remoteRelayBundles
            .filter { RelayBundlePolicy.validateForRelay(it) == null }

    @Synchronized
    fun markRemoteRelayBundleUploadSucceeded(bundleHash: String) {
        _network.update { current ->
            current.copy(
                remoteRelayBundles = current.remoteRelayBundles.filterNot { it.bundleHash == bundleHash },
                lastTransferMessage = "A relay bundle was uploaded to the Rust gateway.",
            )
        }
    }

    @Synchronized
    fun markRemoteRelayBundleUploadFailed(message: String) {
        _network.update { it.copy(lastTransferMessage = message) }
    }

    @Synchronized
    fun markRemoteGatewayUploadSucceeded(envelopeId: String) {
        _network.update { current ->
            current.copy(
                remoteGatewayEnvelopes = current.remoteGatewayEnvelopes.filterNot { it.envelopeId == envelopeId },
                submittedReports = current.submittedReports.map { report ->
                    if (report.envelopeId == envelopeId) {
                        report.copy(remoteGatewayStatus = "Uploaded to Rust gateway")
                    } else {
                        report
                    }
                },
                lastTransferMessage = "An anonymized report was uploaded to the Rust gateway.",
            )
        }
    }

    @Synchronized
    fun markRemoteGatewayUploadFailed(envelopeId: String, message: String) {
        _network.update { current ->
            current.copy(
                submittedReports = current.submittedReports.map { report ->
                    if (report.envelopeId == envelopeId) {
                        report.copy(remoteGatewayStatus = "Rust gateway pending")
                    } else {
                        report
                    }
                },
                lastTransferMessage = message,
            )
        }
    }

    fun receiveNearbyReportPacket(
        packet: WireReportPacket,
        fromAlias: String,
    ): TransferResult {
        val result = ReportingDeviceTransfer.receivePacket(packet, fromAlias)
        _network.update {
            if (result.delivered && result.receivedItem != null) {
                it.copy(
                    receivedReports = listOf(result.receivedItem) + it.receivedReports,
                    lastTransferMessage = "An anonymized report was received from $fromAlias.",
                )
            } else {
                it.copy(lastTransferMessage = result.message)
            }
        }
        return result
    }

    @Synchronized
    fun receiveRelayBundle(
        bundle: RelayBundle,
        fromAlias: String,
    ): Boolean {
        val blockReason = RelayBundlePolicy.validateForRelay(bundle)
        if (blockReason != null) {
            _network.update { it.copy(lastTransferMessage = blockReason) }
            return false
        }

        _network.update { current ->
            val knownHashes = (current.receivedRelayBundles.map { it.bundle.bundleHash } +
                current.pendingRelayBundles.map { it.bundle.bundleHash }).toSet()
            if (bundle.bundleHash in knownHashes) {
                val updatedInbox = current.receivedRelayBundles.map { item ->
                    if (item.bundle.bundleHash == bundle.bundleHash &&
                        fromAlias !in item.receivedFromAliases
                    ) {
                        item.copy(receivedFromAliases = item.receivedFromAliases + fromAlias)
                    } else {
                        item
                    }
                }
                val peerCount = updatedInbox
                    .firstOrNull { it.bundle.bundleHash == bundle.bundleHash }
                    ?.peerCount ?: 1
                current.copy(
                    receivedRelayBundles = updatedInbox,
                    lastTransferMessage = if (peerCount >= 2) {
                        "Relay alert corroborated by $peerCount nearby peers."
                    } else {
                        "Relay bundle already known; duplicate ignored."
                    },
                )
            } else {
                current.copy(
                    pendingRelayBundles = listOf(RelayBundleCarrierItem(bundle)) + current.pendingRelayBundles,
                    remoteRelayBundles = listOf(bundle) + current.remoteRelayBundles,
                    receivedRelayBundles = listOf(
                        RelayBundleInboxItem(bundle = bundle, receivedFromAliases = listOf(fromAlias)),
                    ) + current.receivedRelayBundles,
                    lastTransferMessage = "Received relay alert from $fromAlias.",
                )
            }
        }
        return true
    }

    @Synchronized
    fun receiveRemoteGatewayReports(items: List<ReceivedReportItem>) {
        if (items.isEmpty()) return

        _network.update { current ->
            val knownPacketIds = current.receivedReports.map { it.packetId }.toSet()
            val newItems = items.filterNot { it.packetId in knownPacketIds }
            if (newItems.isEmpty()) {
                current
            } else {
                current.copy(
                    receivedReports = newItems + current.receivedReports,
                    lastTransferMessage = "Downloaded ${newItems.size} anonymized report(s) from the Rust gateway.",
                )
            }
        }
    }

    @Synchronized
    fun receiveRemoteRelayBundles(bundles: List<RelayBundle>) {
        bundles.forEach { receiveRelayBundle(it, "Rust relay gateway") }
    }

    fun shareNextReportToNearbyDevice(): TransferResult {
        val batch = shareNextReportToNearbyDevices()
        return batch.results.firstOrNull()
            ?: TransferResult(batch.deliveredCount > 0, batch.message)
    }

    fun shareNextReportToNearbyDevices(): TransferBatchResult {
        val current = _network.value
        val envelope = current.pendingEnvelopes.firstOrNull()
            ?: return emptyTransfer("No report envelope is waiting to share.")
        return shareReportToNearbyDevices(envelope.envelopeId)
    }

    @Synchronized
    fun createNearbyRelayBundlesForNextBundle(): List<RelayBundle> {
        val current = _network.value
        val carrier = current.pendingRelayBundles.firstOrNull() ?: return emptyList()
        if (RelayBundlePolicy.validateForRelay(carrier.bundle) != null) return emptyList()

        val targets = current.trustedNearbyDevices
            .filter { it.deviceAlias !in carrier.deliveredDeviceAliases }
            .filter { it.deviceAlias !in carrier.sendingDeviceAliases }
            .take(1)

        if (targets.isEmpty()) return emptyList()

        val aliases = targets.map { it.deviceAlias }
        _network.update { state ->
            state.copy(
                pendingRelayBundles = state.pendingRelayBundles.map { pending ->
                    if (pending.bundle.bundleHash == carrier.bundle.bundleHash) {
                        pending.copy(sendingDeviceAliases = (pending.sendingDeviceAliases + aliases).distinct())
                    } else {
                        pending
                    }
                },
            )
        }
        return targets.map { carrier.bundle }
    }

    @Synchronized
    fun createNearbyReportPacketsForNextReport(): List<WireReportPacket> {
        val current = _network.value
        val envelope = current.pendingEnvelopes.firstOrNull()
            ?: return emptyList()
        val remainingSlots = envelope.maxUniqueDevices - envelope.deliveredDeviceAliases.size
        if (remainingSlots <= 0 || envelope.isStale()) return emptyList()

        val packets = current.trustedNearbyDevices
            .filter { it.deviceAlias !in envelope.deliveredDeviceAliases }
            .filter { it.deviceAlias !in envelope.sendingDeviceAliases }
            .take(remainingSlots)
            .map { target -> ReportingDeviceTransfer.createPacketForDevice(envelope, target) }
            .mapNotNull { it.packet }

        if (packets.isNotEmpty()) {
            val aliases = packets.map { it.targetAlias }
            _network.update { state ->
                state.copy(
                    pendingEnvelopes = state.pendingEnvelopes.map { pending ->
                        if (pending.envelopeId == envelope.envelopeId) {
                            pending.copy(sendingDeviceAliases = (pending.sendingDeviceAliases + aliases).distinct())
                        } else {
                            pending
                        }
                    },
                )
            }
        }
        return packets
    }

    @Synchronized
    fun markNearbyReportPacketsSent(packets: List<WireReportPacket>) {
        if (packets.isEmpty()) return

        _network.update { current ->
            val packetsByEnvelope = packets.groupBy { it.sourceEnvelopeId }
            val updatedEnvelopes = current.pendingEnvelopes.map { envelope ->
                val deliveredAliases = packetsByEnvelope[envelope.envelopeId]
                    ?.map { packet -> packet.targetAlias }
                    ?.filterNot { alias -> alias in envelope.deliveredDeviceAliases }
                    ?: emptyList()
                if (deliveredAliases.isEmpty()) {
                    envelope.copy(
                        sendingDeviceAliases = envelope.sendingDeviceAliases.filterNot { alias ->
                            alias in (packetsByEnvelope[envelope.envelopeId]?.map { packet -> packet.targetAlias } ?: emptyList())
                        },
                    )
                } else {
                    envelope.copy(
                        deliveredDeviceAliases = (envelope.deliveredDeviceAliases + deliveredAliases).distinct(),
                        sendingDeviceAliases = envelope.sendingDeviceAliases.filterNot { alias -> alias in deliveredAliases },
                    )
                }
            }
            val remainingPending = updatedEnvelopes.filterNot { envelope ->
                envelope.deliveredDeviceAliases.size >= envelope.maxUniqueDevices || envelope.isStale()
            }
            val updatedSubmittedReports = current.submittedReports.map { report ->
                val envelope = updatedEnvelopes.firstOrNull { it.envelopeId == report.envelopeId }
                if (envelope == null || report.envelopeId !in packetsByEnvelope) {
                    report
                } else {
                    envelope.toSubmittedStatus(
                        "Report sent anonymously to ${envelope.deliveredDeviceAliases.size}/${envelope.maxUniqueDevices} unique devices.",
                        report.remoteGatewayStatus,
                    )
                }
            }
            current.copy(
                pendingEnvelopes = remainingPending,
                queueSize = remainingPending.size,
                queueItems = current.queueItems.map { item ->
                    val envelope = updatedEnvelopes.firstOrNull { it.envelopeId == item.id }
                    if (envelope == null || item.id !in packetsByEnvelope) {
                        item
                    } else {
                        item.copy(
                            status = "Report sent anonymously to ${envelope.deliveredDeviceAliases.size}/${envelope.maxUniqueDevices} unique devices.",
                        )
                    }
                },
                submittedReports = updatedSubmittedReports,
                lastTransferMessage = "Report sent anonymously to ${packets.size} nearby device(s).",
            )
        }
    }

    @Synchronized
    fun markNearbyReportPacketsFailed(packets: List<WireReportPacket>) {
        if (packets.isEmpty()) return
        _network.update { current ->
            val aliasesByEnvelope = packets.groupBy { it.sourceEnvelopeId }
                .mapValues { entry -> entry.value.map { packet -> packet.targetAlias } }
            current.copy(
                pendingEnvelopes = current.pendingEnvelopes.map { envelope ->
                    val failedAliases = aliasesByEnvelope[envelope.envelopeId].orEmpty()
                    if (failedAliases.isEmpty()) {
                        envelope
                    } else {
                        envelope.copy(
                            sendingDeviceAliases = envelope.sendingDeviceAliases.filterNot { alias ->
                                alias in failedAliases
                            },
                        )
                    }
                },
                lastTransferMessage = "Nearby delivery did not complete. Jirani will retry when the device is available.",
            )
        }
    }

    @Synchronized
    fun markNearbyRelayBundlesSent(bundles: List<RelayBundle>) {
        if (bundles.isEmpty()) return
        val hashes = bundles.map { it.bundleHash }.toSet()
        _network.update { current ->
            val updated = current.pendingRelayBundles.map { carrier ->
                if (carrier.bundle.bundleHash !in hashes) {
                    carrier
                } else {
                    carrier.copy(
                        deliveredDeviceAliases = (carrier.deliveredDeviceAliases + carrier.sendingDeviceAliases).distinct(),
                        sendingDeviceAliases = emptyList(),
                    )
                }
            }
            current.copy(
                pendingRelayBundles = updated.filter { it.deliveredDeviceAliases.size < 5 },
                lastTransferMessage = "Relayed ${bundles.size} public safety bundle(s) to nearby devices.",
            )
        }
    }

    @Synchronized
    fun markNearbyRelayBundlesFailed(bundles: List<RelayBundle>) {
        if (bundles.isEmpty()) return
        val hashes = bundles.map { it.bundleHash }.toSet()
        _network.update { current ->
            current.copy(
                pendingRelayBundles = current.pendingRelayBundles.map { carrier ->
                    if (carrier.bundle.bundleHash in hashes) {
                        carrier.copy(sendingDeviceAliases = emptyList())
                    } else {
                        carrier
                    }
                },
                lastTransferMessage = "Relay bundle handoff did not complete. Jirani will retry later.",
            )
        }
    }

    private fun shareReportToNearbyDevices(
        envelopeId: String,
        recordReceivedReports: Boolean = true,
    ): TransferBatchResult {
        val current = _network.value
        val envelope = current.pendingEnvelopes.firstOrNull { it.envelopeId == envelopeId }
            ?: return emptyTransfer("No report envelope is waiting to share.")
        val result = ReportingDeviceTransfer.sendToDevices(envelope, current.trustedNearbyDevices)
        _network.update {
            if (result.deliveredCount == 0) {
                it.copy(lastTransferMessage = result.message)
            } else {
                val completed = result.updatedEnvelope.deliveredDeviceAliases.size >= result.updatedEnvelope.maxUniqueDevices ||
                    result.updatedEnvelope.isStale()
                val updatedPending = if (completed) {
                    it.pendingEnvelopes.filterNot { pending -> pending.envelopeId == envelope.envelopeId }
                } else {
                    it.pendingEnvelopes.map { pending ->
                        if (pending.envelopeId == envelope.envelopeId) result.updatedEnvelope else pending
                    }
                }
                it.copy(
                    pendingEnvelopes = updatedPending,
                    queueSize = updatedPending.size,
                    queueItems = it.queueItems.map { item ->
                        if (item.id == envelope.envelopeId) {
                            item.copy(status = result.message)
                        } else {
                            item
                        }
                    },
                    submittedReports = it.submittedReports.map { report ->
                        if (report.envelopeId == envelope.envelopeId) {
                            result.updatedEnvelope.toSubmittedStatus(result.message, report.remoteGatewayStatus)
                        } else {
                            report
                        }
                    },
                    receivedReports = if (recordReceivedReports) {
                        result.results.mapNotNull { transfer -> transfer.receivedItem } + it.receivedReports
                    } else {
                        it.receivedReports
                    },
                    lastTransferMessage = result.message,
                )
            }
        }
        return result
    }

    fun updateDiscreetCode(code: String) {
        _securitySettings.update { it.copy(discreetCode = code) }
    }

    fun updateNearbySharingEnabled(enabled: Boolean) {
        _securitySettings.update { it.copy(nearbySharingEnabled = enabled) }
    }

    fun updateActiveRelayModeEnabled(enabled: Boolean) {
        _securitySettings.update { it.copy(activeRelayModeEnabled = enabled) }
        if (enabled) {
            startRelayForegroundService()
        } else {
            stopRelayForegroundService()
        }
    }

    fun updateLanguage(language: AppLanguage) {
        _securitySettings.update { it.copy(language = language) }
    }

    fun updateThemeMode(themeMode: AppThemeMode) {
        _securitySettings.update { it.copy(themeMode = themeMode) }
    }

    private fun SyncEnvelope.queueStatusLabel(): String = when (audienceTier) {
        SyncAudienceTier.SurvivorSupportOnly -> "Held locally; survivor-support sharing only"
        SyncAudienceTier.ProtectionActors -> "Waiting for trusted protection contact"
        SyncAudienceTier.TrustedVerifier -> "Ready for nearby trusted verifier"
        SyncAudienceTier.CommunityAlert -> "Ready for community alert sharing"
    }

    private fun SyncEnvelope.toSubmittedStatus(
        status: String,
        remoteGatewayStatus: String = RemoteGatewaySyncPolicy.uploadStatusLabel(this),
    ): SubmittedReportStatus =
        SubmittedReportStatus(
            envelopeId = envelopeId,
            title = "${payload.reportType} report",
            deliveredCount = deliveredDeviceAliases.size,
            maxDevices = maxUniqueDevices,
            stale = isStale(),
            status = status,
            remoteGatewayStatus = remoteGatewayStatus,
        )

    private fun trustedDemoDevices(): List<NearbyJiraniDevice> =
        listOf(
            "Elder verifier phone",
            "Peace committee phone",
            "Chief desk phone",
            "OSF partner phone",
            "Clinic support phone",
        ).map { alias ->
            NearbyJiraniDevice(
                deviceAlias = alias,
                trusted = true,
                supportedTransports = listOf(
                    SyncTransport.NearbyConnections,
                    SyncTransport.WifiDirect,
                    SyncTransport.AndroidShareSheet,
                ),
            )
        }

    private fun emptyTransfer(message: String): TransferBatchResult =
        TransferBatchResult(
            deliveredCount = 0,
            attemptedCount = 0,
            updatedEnvelope = SyncEnvelope(
                envelopeId = "none",
                recordType = "none",
                recordId = "none",
                contentHash = "none",
                version = 0,
                lastModifiedBucket = "none",
                audienceTier = SyncAudienceTier.TrustedVerifier,
                syncState = SyncState.LocalHold,
                allowedTransports = emptyList(),
                payload = SanitizedReportPayload(
                    reportType = "none",
                    generalArea = "none",
                    timeWindow = "none",
                    submittedAtEpochSeconds = 0,
                    observedRisk = "none",
                    verificationStatus = VerificationStatus.LocalOnly,
                    sensitivity = ReportSensitivity.Community,
                ),
                expiresAtEpochSeconds = 0,
            ),
            results = emptyList(),
            message = message,
        )

    private fun mergeCarrierItems(
        primary: List<RelayBundleCarrierItem>,
        secondary: List<RelayBundleCarrierItem>,
    ): List<RelayBundleCarrierItem> =
        (primary + secondary).groupBy { it.bundle.bundleHash }.map { (_, items) ->
            val first = items.first()
            first.copy(
                deliveredDeviceAliases = items.flatMap { it.deliveredDeviceAliases }.distinct(),
                sendingDeviceAliases = emptyList(),
            )
        }

    private fun mergeInboxItems(
        primary: List<RelayBundleInboxItem>,
        secondary: List<RelayBundleInboxItem>,
    ): List<RelayBundleInboxItem> =
        (primary + secondary).groupBy { it.bundle.bundleHash }.map { (_, items) ->
            val first = items.first()
            first.copy(receivedFromAliases = items.flatMap { it.receivedFromAliases }.distinct())
        }

    private fun encodeNetworkSnapshot(snapshot: NetworkSnapshot): String =
        JSONObject()
            .put("queueItems", JSONArray(snapshot.queueItems.map { it.toJson() }))
            .put("pendingEnvelopes", JSONArray(snapshot.pendingEnvelopes.map { it.copy(sendingDeviceAliases = emptyList()).toJson() }))
            .put("remoteGatewayEnvelopes", JSONArray(snapshot.remoteGatewayEnvelopes.map { it.copy(sendingDeviceAliases = emptyList()).toJson() }))
            .put("pendingRelayBundles", JSONArray(snapshot.pendingRelayBundles.map { it.copy(sendingDeviceAliases = emptyList()).toJson() }))
            .put("remoteRelayBundles", JSONArray(snapshot.remoteRelayBundles.map { it.toJson() }))
            .put("receivedReports", JSONArray(snapshot.receivedReports.map { it.toJson() }))
            .put("receivedRelayBundles", JSONArray(snapshot.receivedRelayBundles.map { it.toJson() }))
            .put("submittedReports", JSONArray(snapshot.submittedReports.map { it.toJson() }))
            .put("lastTransferMessage", snapshot.lastTransferMessage)
            .toString()

    private fun encodeSecuritySettings(settings: SecuritySettings): String =
        JSONObject()
            .put("discreetCode", settings.discreetCode)
            .put("nearbySharingEnabled", settings.nearbySharingEnabled)
            .put("activeRelayModeEnabled", settings.activeRelayModeEnabled)
            .put("language", settings.language.name)
            .put("themeMode", settings.themeMode.name)
            .toString()

    private fun decodeSecuritySettings(encoded: String): SecuritySettings {
        val json = JSONObject(encoded)
        return SecuritySettings(
            discreetCode = json.optString("discreetCode", "2468="),
            nearbySharingEnabled = json.optBoolean("nearbySharingEnabled", true),
            activeRelayModeEnabled = json.optBoolean("activeRelayModeEnabled", false),
            language = json.optEnum("language", AppLanguage.English),
            themeMode = json.optEnum("themeMode", AppThemeMode.Light),
        )
    }

    fun isActiveRelayModeEnabled(): Boolean =
        _securitySettings.value.activeRelayModeEnabled

    private fun startRelayForegroundService() {
        val context = appContext ?: return
        ContextCompat.startForegroundService(
            context,
            Intent(context, RelayForegroundService::class.java),
        )
    }

    private fun stopRelayForegroundService() {
        val context = appContext ?: return
        context.stopService(Intent(context, RelayForegroundService::class.java))
    }

    private fun decodeNetworkSnapshot(encoded: String): NetworkSnapshot {
        val json = JSONObject(encoded)
        val pendingEnvelopes = json.optJSONArray("pendingEnvelopes").toList { it.toSyncEnvelope() }
        val remoteGatewayEnvelopes = json.optJSONArray("remoteGatewayEnvelopes").toList { it.toSyncEnvelope() }
        val queueItems = json.optJSONArray("queueItems").toList { it.toSyncQueueItem() }
        return NetworkSnapshot(
            queueSize = pendingEnvelopes.size,
            queueItems = queueItems,
            pendingEnvelopes = pendingEnvelopes,
            remoteGatewayEnvelopes = remoteGatewayEnvelopes,
            pendingRelayBundles = json.optJSONArray("pendingRelayBundles").toList { it.toRelayBundleCarrierItem() },
            remoteRelayBundles = json.optJSONArray("remoteRelayBundles").toList { it.toRelayBundle() },
            receivedReports = json.optJSONArray("receivedReports").toList { it.toReceivedReportItem() },
            receivedRelayBundles = json.optJSONArray("receivedRelayBundles").toList { it.toRelayBundleInboxItem() },
            submittedReports = json.optJSONArray("submittedReports").toList { it.toSubmittedReportStatus() },
            lastTransferMessage = json.optString("lastTransferMessage").ifBlank { null },
        )
    }

    private fun SyncQueueItem.toJson(): JSONObject =
        JSONObject()
            .put("id", id)
            .put("title", title)
            .put("status", status)

    private fun JSONObject.toSyncQueueItem(): SyncQueueItem =
        SyncQueueItem(
            id = optString("id"),
            title = optString("title"),
            status = optString("status"),
        )

    private fun SubmittedReportStatus.toJson(): JSONObject =
        JSONObject()
            .put("envelopeId", envelopeId)
            .put("title", title)
            .put("deliveredCount", deliveredCount)
            .put("maxDevices", maxDevices)
            .put("stale", stale)
            .put("status", status)
            .put("remoteGatewayStatus", remoteGatewayStatus)

    private fun JSONObject.toSubmittedReportStatus(): SubmittedReportStatus =
        SubmittedReportStatus(
            envelopeId = optString("envelopeId"),
            title = optString("title"),
            deliveredCount = optInt("deliveredCount"),
            maxDevices = optInt("maxDevices", 5),
            stale = optBoolean("stale"),
            status = optString("status"),
            remoteGatewayStatus = optString("remoteGatewayStatus", "Waiting for Rust gateway"),
        )

    private fun ReceivedReportItem.toJson(): JSONObject =
        JSONObject()
            .put("packetId", packetId)
            .put("fromAlias", fromAlias)
            .put("transport", transport.name)
            .put("reportType", reportType)
            .put("generalArea", generalArea)
            .put("timeWindow", timeWindow)
            .put("submittedAtEpochSeconds", submittedAtEpochSeconds)
            .put("observedRisk", observedRisk)
            .put("verificationStatus", verificationStatus.name)
            .put("sensitivity", sensitivity.name)

    private fun JSONObject.toReceivedReportItem(): ReceivedReportItem =
        ReceivedReportItem(
            packetId = optString("packetId"),
            fromAlias = optString("fromAlias"),
            transport = optEnum("transport", SyncTransport.NearbyConnections),
            reportType = optString("reportType"),
            generalArea = optString("generalArea"),
            timeWindow = optString("timeWindow"),
            submittedAtEpochSeconds = optLong("submittedAtEpochSeconds"),
            observedRisk = optString("observedRisk"),
            verificationStatus = optEnum("verificationStatus", VerificationStatus.PendingVerification),
            sensitivity = optEnum("sensitivity", ReportSensitivity.Community),
        )

    private fun RelayBundleCarrierItem.toJson(): JSONObject =
        JSONObject()
            .put("bundle", bundle.toJson())
            .put("deliveredDeviceAliases", JSONArray(deliveredDeviceAliases))

    private fun JSONObject.toRelayBundleCarrierItem(): RelayBundleCarrierItem =
        RelayBundleCarrierItem(
            bundle = optJSONObject("bundle")?.toRelayBundle() ?: toRelayBundle(),
            deliveredDeviceAliases = optJSONArray("deliveredDeviceAliases").toStringList(),
        )

    private fun RelayBundleInboxItem.toJson(): JSONObject =
        JSONObject()
            .put("bundle", bundle.toJson())
            .put("receivedFromAliases", JSONArray(receivedFromAliases))

    private fun JSONObject.toRelayBundleInboxItem(): RelayBundleInboxItem =
        RelayBundleInboxItem(
            bundle = optJSONObject("bundle")?.toRelayBundle() ?: toRelayBundle(),
            receivedFromAliases = optJSONArray("receivedFromAliases").toStringList(),
        )

    private fun RelayBundle.toJson(): JSONObject =
        JSONObject()
            .put("bundleId", bundleId)
            .put("publicHeader", publicHeader.toJson())
            .put("encryptedPayload", encryptedPayload)
            .put("payloadHash", payloadHash)
            .put("bundleHash", bundleHash)
            .put("expiresAtEpochSeconds", expiresAtEpochSeconds)

    private fun JSONObject.toRelayBundle(): RelayBundle =
        RelayBundle(
            bundleId = optString("bundleId"),
            publicHeader = optJSONObject("publicHeader")?.toRelayPublicHeader() ?: RelayPublicHeader(
                alertType = "unknown",
                generalArea = "general area withheld",
                timeWindow = "time window not specified",
                riskLevel = "Reported",
                message = "details withheld",
                verificationStatus = VerificationStatus.PendingVerification,
                audienceTier = SyncAudienceTier.TrustedVerifier,
                sensitivity = ReportSensitivity.Community,
            ),
            encryptedPayload = optString("encryptedPayload"),
            payloadHash = optString("payloadHash"),
            bundleHash = optString("bundleHash"),
            expiresAtEpochSeconds = optLong("expiresAtEpochSeconds"),
        )

    private fun RelayPublicHeader.toJson(): JSONObject =
        JSONObject()
            .put("alertType", alertType)
            .put("generalArea", generalArea)
            .put("timeWindow", timeWindow)
            .put("riskLevel", riskLevel)
            .put("message", message)
            .put("verificationStatus", verificationStatus.name)
            .put("audienceTier", audienceTier.name)
            .put("sensitivity", sensitivity.name)

    private fun JSONObject.toRelayPublicHeader(): RelayPublicHeader =
        RelayPublicHeader(
            alertType = optString("alertType"),
            generalArea = optString("generalArea"),
            timeWindow = optString("timeWindow"),
            riskLevel = optString("riskLevel", "Reported"),
            message = optString("message"),
            verificationStatus = optEnum("verificationStatus", VerificationStatus.PendingVerification),
            audienceTier = optEnum("audienceTier", SyncAudienceTier.TrustedVerifier),
            sensitivity = optEnum("sensitivity", ReportSensitivity.Community),
        )

    private fun SyncEnvelope.toJson(): JSONObject =
        JSONObject()
            .put("envelopeId", envelopeId)
            .put("recordType", recordType)
            .put("recordId", recordId)
            .put("contentHash", contentHash)
            .put("version", version)
            .put("lastModifiedBucket", lastModifiedBucket)
            .put("audienceTier", audienceTier.name)
            .put("syncState", syncState.name)
            .put("allowedTransports", JSONArray(allowedTransports.map { it.name }))
            .put("payload", payload.toJson())
            .put("expiresAtEpochSeconds", expiresAtEpochSeconds)
            .put("deliveredDeviceAliases", JSONArray(deliveredDeviceAliases))
            .put("maxUniqueDevices", maxUniqueDevices)

    private fun JSONObject.toSyncEnvelope(): SyncEnvelope =
        SyncEnvelope(
            envelopeId = optString("envelopeId"),
            recordType = optString("recordType"),
            recordId = optString("recordId"),
            contentHash = optString("contentHash"),
            version = optInt("version", 1),
            lastModifiedBucket = optString("lastModifiedBucket"),
            audienceTier = optEnum("audienceTier", SyncAudienceTier.TrustedVerifier),
            syncState = optEnum("syncState", SyncState.LocalHold),
            allowedTransports = optJSONArray("allowedTransports").toEnumList<SyncTransport>(),
            payload = optJSONObject("payload")?.toSanitizedReportPayload() ?: SanitizedReportPayload(
                reportType = "unknown",
                generalArea = "general area withheld",
                timeWindow = "time window not specified",
                submittedAtEpochSeconds = 0,
                observedRisk = "details withheld",
                verificationStatus = VerificationStatus.PendingVerification,
                sensitivity = ReportSensitivity.Community,
            ),
            expiresAtEpochSeconds = optLong("expiresAtEpochSeconds"),
            deliveredDeviceAliases = optJSONArray("deliveredDeviceAliases").toStringList(),
            maxUniqueDevices = optInt("maxUniqueDevices", 5),
        )

    private fun SanitizedReportPayload.toJson(): JSONObject =
        JSONObject()
            .put("reportType", reportType)
            .put("generalArea", generalArea)
            .put("timeWindow", timeWindow)
            .put("submittedAtEpochSeconds", submittedAtEpochSeconds)
            .put("observedRisk", observedRisk)
            .put("verificationStatus", verificationStatus.name)
            .put("sensitivity", sensitivity.name)

    private fun JSONObject.toSanitizedReportPayload(): SanitizedReportPayload =
        SanitizedReportPayload(
            reportType = optString("reportType"),
            generalArea = optString("generalArea"),
            timeWindow = optString("timeWindow"),
            submittedAtEpochSeconds = optLong("submittedAtEpochSeconds"),
            observedRisk = optString("observedRisk"),
            verificationStatus = optEnum("verificationStatus", VerificationStatus.PendingVerification),
            sensitivity = optEnum("sensitivity", ReportSensitivity.Community),
        )

    private fun JSONArray?.toStringList(): List<String> =
        if (this == null) {
            emptyList()
        } else {
            (0 until length()).mapNotNull { index -> optString(index).takeIf { it.isNotBlank() } }
        }

    private inline fun <reified T : Enum<T>> JSONArray?.toEnumList(): List<T> =
        if (this == null) {
            emptyList()
        } else {
            (0 until length()).mapNotNull { index ->
                runCatching { enumValueOf<T>(optString(index)) }.getOrNull()
            }
        }

    private fun <T> JSONArray?.toList(transform: (JSONObject) -> T): List<T> =
        if (this == null) {
            emptyList()
        } else {
            (0 until length()).mapNotNull { index -> optJSONObject(index)?.let(transform) }
        }

    private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, fallback: T): T =
        runCatching { enumValueOf<T>(optString(key)) }.getOrDefault(fallback)

    private const val StoreName = "jirani_local_first_store"
    private const val NetworkSnapshotKey = "network_snapshot_v1"
    private const val SecuritySettingsKey = "security_settings_v1"
}
