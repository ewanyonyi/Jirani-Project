package com.jirani.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class SyncStatus {
    Local,
    Mesh,
    Cloud,
}

data class AgreementItem(
    val id: String,
    val title: String,
    val summary: String,
    val syncStatus: SyncStatus,
    val encrypted: Boolean = true,
)

data class NetworkSnapshot(
    val peerDetected: Boolean = false,
    val nearbyNeighbors: Int = 0,
    val queueSize: Int = 0,
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
            ),
            AgreementItem(
                id = "grazing-route",
                title = "Grazing Route Record",
                summary = "Community members keep a temporary boundary route while facts are verified.",
                syncStatus = SyncStatus.Mesh,
            ),
        ),
    )
    val agreements: StateFlow<List<AgreementItem>> = _agreements

    private val _network = MutableStateFlow(
        NetworkSnapshot(
            peerDetected = false,
            nearbyNeighbors = 0,
            queueSize = 2,
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
                ),
            ) + current
        }
        enqueueSync()
    }

    fun enqueueSync() {
        _network.update { it.copy(queueSize = it.queueSize + 1) }
    }

    fun togglePeerSimulation() {
        _network.update {
            val nextDetected = !it.peerDetected
            it.copy(
                peerDetected = nextDetected,
                nearbyNeighbors = if (nextDetected) 3 else 0,
            )
        }
    }

    fun updateDiscreetCode(code: String) {
        _securitySettings.update { it.copy(discreetCode = code) }
    }
}
