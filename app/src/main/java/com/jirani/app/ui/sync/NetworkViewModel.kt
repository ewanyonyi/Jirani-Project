package com.jirani.app.ui.sync

import androidx.lifecycle.ViewModel
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.data.local.WireReportPacket
import com.jirani.app.sync.NearbyScanSnapshot

class NetworkViewModel : ViewModel() {
    val network = LocalFirstUiStore.network

    fun updateNearbyScan(scan: NearbyScanSnapshot) {
        LocalFirstUiStore.updateNearbyDevices(
            devices = scan.connectedDevices,
            scanning = scan.scanning,
        )
    }

    fun shareNextReport() {
        LocalFirstUiStore.shareNextReportToNearbyDevices()
    }

    fun shareNextReportPackets(): List<WireReportPacket> =
        LocalFirstUiStore.createNearbyReportPacketsForNextReport()

    fun markReportPacketsSent(packets: List<WireReportPacket>) {
        LocalFirstUiStore.markNearbyReportPacketsSent(packets)
    }

    fun hasWaitingReports(): Boolean =
        LocalFirstUiStore.network.value.pendingEnvelopes.isNotEmpty()

    fun receiveNearbyReportPacket(
        packet: WireReportPacket,
        fromAlias: String,
    ) {
        LocalFirstUiStore.receiveNearbyReportPacket(packet, fromAlias)
    }
}
