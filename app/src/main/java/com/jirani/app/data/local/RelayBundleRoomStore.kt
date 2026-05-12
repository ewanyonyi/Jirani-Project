package com.jirani.app.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "relay_bundles")
data class RelayBundleEntity(
    @PrimaryKey val bundleHash: String,
    val bundleJson: String,
    val deliveredAliasesJson: String,
    val receivedFromAliasesJson: String,
    val pendingLocalRelay: Boolean,
    val pendingRemoteSync: Boolean,
    val updatedAtEpochSeconds: Long,
)

@Dao
interface RelayBundleDao {
    @Query("SELECT * FROM relay_bundles ORDER BY updatedAtEpochSeconds DESC")
    suspend fun all(): List<RelayBundleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RelayBundleEntity)
}

@Database(
    entities = [RelayBundleEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class JiraniRoomDatabase : RoomDatabase() {
    abstract fun relayBundleDao(): RelayBundleDao
}

object RelayBundleRoomStore {
    @Volatile
    private var database: JiraniRoomDatabase? = null

    fun initialize(context: Context) {
        if (database != null) return
        synchronized(this) {
            if (database == null) {
                database = Room.databaseBuilder(
                    context.applicationContext,
                    JiraniRoomDatabase::class.java,
                    "jirani-room.db",
                ).build()
            }
        }
    }

    suspend fun load(): RelayRoomSnapshot {
        val dao = database?.relayBundleDao() ?: return RelayRoomSnapshot()
        val entities = dao.all()
        return RelayRoomSnapshot(
            pendingRelayBundles = entities
                .filter { it.pendingLocalRelay }
                .mapNotNull { it.toCarrierItem() },
            remoteRelayBundles = entities
                .filter { it.pendingRemoteSync }
                .mapNotNull { it.toRelayBundle() },
            receivedRelayBundles = entities
                .filter { it.receivedFromAliasesJson.toStringList().isNotEmpty() }
                .mapNotNull { it.toInboxItem() },
        )
    }

    suspend fun save(snapshot: NetworkSnapshot) {
        val dao = database?.relayBundleDao() ?: return
        val pendingByHash = snapshot.pendingRelayBundles.associateBy { it.bundle.bundleHash }
        val remoteHashes = snapshot.remoteRelayBundles.map { it.bundleHash }.toSet()
        val inboxByHash = snapshot.receivedRelayBundles.associateBy { it.bundle.bundleHash }
        val bundles = (
            pendingByHash.values.map { it.bundle } +
                snapshot.remoteRelayBundles +
                inboxByHash.values.map { it.bundle }
            ).distinctBy { it.bundleHash }

        bundles.forEach { bundle ->
            val pending = pendingByHash[bundle.bundleHash]
            val inbox = inboxByHash[bundle.bundleHash]
            dao.upsert(
                RelayBundleEntity(
                    bundleHash = bundle.bundleHash,
                    bundleJson = bundle.toJson().toString(),
                    deliveredAliasesJson = JSONArray(pending?.deliveredDeviceAliases.orEmpty()).toString(),
                    receivedFromAliasesJson = JSONArray(inbox?.receivedFromAliases.orEmpty()).toString(),
                    pendingLocalRelay = pending != null,
                    pendingRemoteSync = bundle.bundleHash in remoteHashes,
                    updatedAtEpochSeconds = System.currentTimeMillis() / 1_000,
                ),
            )
        }
    }

    private fun RelayBundleEntity.toRelayBundle(): RelayBundle? =
        runCatching { JSONObject(bundleJson).toRelayBundle() }.getOrNull()

    private fun RelayBundleEntity.toCarrierItem(): RelayBundleCarrierItem? =
        toRelayBundle()?.let { bundle ->
            RelayBundleCarrierItem(
                bundle = bundle,
                deliveredDeviceAliases = deliveredAliasesJson.toStringList(),
            )
        }

    private fun RelayBundleEntity.toInboxItem(): RelayBundleInboxItem? =
        toRelayBundle()?.let { bundle ->
            RelayBundleInboxItem(
                bundle = bundle,
                receivedFromAliases = receivedFromAliasesJson.toStringList(),
            )
        }

    private fun String.toStringList(): List<String> =
        runCatching {
            val array = JSONArray(this)
            (0 until array.length()).mapNotNull { index ->
                array.optString(index).takeIf { it.isNotBlank() }
            }
        }.getOrDefault(emptyList())
}

data class RelayRoomSnapshot(
    val pendingRelayBundles: List<RelayBundleCarrierItem> = emptyList(),
    val remoteRelayBundles: List<RelayBundle> = emptyList(),
    val receivedRelayBundles: List<RelayBundleInboxItem> = emptyList(),
)

fun RelayBundle.toJson(): JSONObject =
    JSONObject()
        .put("bundleId", bundleId)
        .put("publicHeader", publicHeader.toJson())
        .put("encryptedPayload", encryptedPayload)
        .put("payloadHash", payloadHash)
        .put("bundleHash", bundleHash)
        .put("expiresAtEpochSeconds", expiresAtEpochSeconds)

fun JSONObject.toRelayBundle(): RelayBundle =
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

fun RelayPublicHeader.toJson(): JSONObject =
    JSONObject()
        .put("alertType", alertType)
        .put("generalArea", generalArea)
        .put("timeWindow", timeWindow)
        .put("riskLevel", riskLevel)
        .put("message", message)
        .put("verificationStatus", verificationStatus.name)
        .put("audienceTier", audienceTier.name)
        .put("sensitivity", sensitivity.name)

fun JSONObject.toRelayPublicHeader(): RelayPublicHeader =
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

private inline fun <reified T : Enum<T>> JSONObject.optEnum(key: String, fallback: T): T =
    runCatching { enumValueOf<T>(optString(key)) }.getOrDefault(fallback)
