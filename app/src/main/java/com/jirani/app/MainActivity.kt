package com.jirani.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jirani.app.data.local.LocalFirstUiStore
import com.jirani.app.sync.NearbySyncRuntime
import com.jirani.app.sync.RemoteGatewaySyncRuntime
import com.jirani.app.ui.JiraniApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalFirstUiStore.initializePersistence(applicationContext)
        NearbySyncRuntime.initialize(applicationContext)
        RemoteGatewaySyncRuntime.initialize()
        enableEdgeToEdge()
        setContent {
            JiraniApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            NearbySyncRuntime.stop()
        }
    }
}
