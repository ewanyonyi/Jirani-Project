package com.jirani.app.sync

import com.jirani.app.data.local.SyncAudienceTier
import com.jirani.app.data.local.SyncTransport
import com.jirani.app.data.local.WireReportPacket

object NearbyReportPacketCodec {
    private const val Prefix = "JIRANI_REPORT_PACKET_V1"
    private const val Separator = "\u001E"

    fun encode(packet: WireReportPacket): ByteArray =
        listOf(
            Prefix,
            packet.packetId,
            packet.sourceEnvelopeId,
            packet.targetAlias,
            packet.transport.name,
            packet.audienceTier.name,
            packet.contentHash,
            packet.sealedPayload,
        ).joinToString(Separator).toByteArray()

    fun decode(bytes: ByteArray): WireReportPacket? {
        val parts = bytes.toString(Charsets.UTF_8).split(Separator)
        if (parts.size != 8 || parts[0] != Prefix) return null

        return runCatching {
            WireReportPacket(
                packetId = parts[1],
                sourceEnvelopeId = parts[2],
                targetAlias = parts[3],
                transport = SyncTransport.valueOf(parts[4]),
                audienceTier = SyncAudienceTier.valueOf(parts[5]),
                contentHash = parts[6],
                sealedPayload = parts[7],
            )
        }.getOrNull()
    }
}
