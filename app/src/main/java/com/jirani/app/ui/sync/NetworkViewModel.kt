package com.jirani.app.ui.sync

import androidx.lifecycle.ViewModel
import com.jirani.app.data.local.LocalFirstUiStore

class NetworkViewModel : ViewModel() {
    val network = LocalFirstUiStore.network

    fun togglePeerSimulation() {
        LocalFirstUiStore.togglePeerSimulation()
    }
}
