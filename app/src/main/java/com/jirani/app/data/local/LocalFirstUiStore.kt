package com.jirani.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

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
)

data class NetworkSnapshot(
    val peerDetected: Boolean = false,
    val nearbyNeighbors: Int = 0,
    val queueSize: Int = 0,
    val queueItems: List<SyncQueueItem> = emptyList(),
    val pendingEnvelopes: List<SyncEnvelope> = emptyList(),
    val trustedNearbyDevices: List<NearbyJiraniDevice> = emptyList(),
    val receivedReports: List<ReceivedReportItem> = emptyList(),
    val submittedReports: List<SubmittedReportStatus> = emptyList(),
    val lastTransferMessage: String? = null,
)

data class ReportSubmissionReceipt(
    val envelope: SyncEnvelope,
    val message: String,
)

data class SecuritySettings(
    val discreetCode: String = "2468=",
)

object LocalFirstUiStore {
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
            queueSize = 2,
            queueItems = listOf(
                SyncQueueItem("water-access", "Water access draft", "Waiting to share safely"),
                SyncQueueItem("safety-check", "Safety alert summary", "Saved only on this phone"),
            ),
        ),
    )
    val network: StateFlow<NetworkSnapshot> = _network

    private val _securitySettings = MutableStateFlow(SecuritySettings())
    val securitySettings: StateFlow<SecuritySettings> = _securitySettings

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
                            result.updatedEnvelope.toSubmittedStatus(result.message)
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

    private fun SyncEnvelope.queueStatusLabel(): String = when (audienceTier) {
        SyncAudienceTier.SurvivorSupportOnly -> "Held locally; survivor-support sharing only"
        SyncAudienceTier.ProtectionActors -> "Waiting for trusted protection contact"
        SyncAudienceTier.TrustedVerifier -> "Ready for nearby trusted verifier"
        SyncAudienceTier.CommunityAlert -> "Ready for community alert sharing"
    }

    private fun SyncEnvelope.toSubmittedStatus(status: String): SubmittedReportStatus =
        SubmittedReportStatus(
            envelopeId = envelopeId,
            title = "${payload.reportType} report",
            deliveredCount = deliveredDeviceAliases.size,
            maxDevices = maxUniqueDevices,
            stale = isStale(),
            status = status,
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
}
