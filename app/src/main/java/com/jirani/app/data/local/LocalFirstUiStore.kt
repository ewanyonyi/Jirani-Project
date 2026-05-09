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

data class NetworkSnapshot(
    val peerDetected: Boolean = false,
    val nearbyNeighbors: Int = 0,
    val queueSize: Int = 0,
    val queueItems: List<SyncQueueItem> = emptyList(),
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

    fun togglePeerSimulation() {
        _network.update {
            val nextDetected = !it.peerDetected
            it.copy(
                peerDetected = nextDetected,
                nearbyNeighbors = if (nextDetected) 5 else 0,
            )
        }
    }

    fun updateDiscreetCode(code: String) {
        _securitySettings.update { it.copy(discreetCode = code) }
    }
}
