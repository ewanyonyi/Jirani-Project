package com.jirani.app.ui.sync

import androidx.lifecycle.ViewModel
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.sync.NearbyScanSnapshot

class NetworkViewModel : ViewModel() {
    val network = LocalFirstUiStore.network

    fun updateNearbyScan(scan: NearbyScanSnapshot) {
        LocalFirstUiStore.updateNearbyDevices(
            devices = scan.devices,
            scanning = scan.scanning,
        )
    }

    fun shareNextReport() {
        LocalFirstUiStore.shareNextReportToNearbyDevices()
    }
}
