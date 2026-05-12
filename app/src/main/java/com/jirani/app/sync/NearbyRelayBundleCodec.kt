package com.jirani.app.sync

import com.jirani.app.data.local.RelayBundle
import org.json.JSONObject

object NearbyRelayBundleCodec {
    private const val Prefix = "JIRANI_RELAY_BUNDLE_V1"
    private const val Separator = "\u001E"

    fun encode(bundle: RelayBundle): ByteArray =
        listOf(
            Prefix,
            RelayBundleJsonCodec.encode(bundle).toString(),
        ).joinToString(Separator).toByteArray()

    fun decode(bytes: ByteArray): RelayBundle? {
        val parts = bytes.toString(Charsets.UTF_8).split(Separator, limit = 2)
        if (parts.size != 2 || parts[0] != Prefix) return null
        return RelayBundleJsonCodec.decode(JSONObject(parts[1]))
    }
}
