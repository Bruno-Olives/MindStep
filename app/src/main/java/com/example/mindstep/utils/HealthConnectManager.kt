package com.example.mindstep.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.ZoneId

data class HealthData(
    val steps: Int?,
    val sleepHours: Int?,
    val waterGlasses: Int?
)

class HealthConnectManager(private val context: Context) {

    companion object {
        private const val TAG = "HealthConnect"

        val PERMISSIONS = setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(HydrationRecord::class)
        )
    }

    private val client: HealthConnectClient? by lazy {
        try {
            val status = HealthConnectClient.getSdkStatus(context)
            Log.d(TAG, "SDK status: $status")
            if (status == HealthConnectClient.SDK_AVAILABLE) {
                HealthConnectClient.getOrCreate(context)
            } else {
                Log.w(TAG, "SDK not available, status=$status")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating client", e)
            null
        }
    }

    fun isAvailable(): Boolean {
        return try {
            val status = HealthConnectClient.getSdkStatus(context)
            Log.d(TAG, "isAvailable check: status=$status")
            status == HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            Log.e(TAG, "isAvailable error", e)
            false
        }
    }

    fun getSdkStatusDescription(): String {
        return try {
            when (HealthConnectClient.getSdkStatus(context)) {
                HealthConnectClient.SDK_AVAILABLE -> "Disponível"
                HealthConnectClient.SDK_UNAVAILABLE -> "Indisponível"
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "Atualização necessária"
                else -> "Estado desconhecido"
            }
        } catch (e: Exception) {
            "Erro: ${e.message}"
        }
    }

    suspend fun hasPermissions(): Boolean {
        val hc = client ?: run {
            Log.w(TAG, "hasPermissions: client is null")
            return false
        }
        return try {
            val granted = hc.permissionController.getGrantedPermissions()
            Log.d(TAG, "Granted permissions: $granted")
            Log.d(TAG, "Required permissions: $PERMISSIONS")
            val hasAll = PERMISSIONS.all { it in granted }
            Log.d(TAG, "Has all permissions: $hasAll")
            hasAll
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            false
        }
    }

    /**
     * Opens the Health Connect app settings so the user can manually grant permissions.
     */
    fun openHealthConnectSettings(context: Context) {
        try {
            val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open HC settings, trying fallback", e)
            try {
                // Fallback: open Health Connect app directly
                val intent = context.packageManager.getLaunchIntentForPackage(
                    "com.google.android.apps.healthdata"
                )
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    // Last resort: open Play Store
                    val storeIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(storeIntent)
                }
            } catch (e2: Exception) {
                Log.e(TAG, "All fallbacks failed", e2)
            }
        }
    }

    suspend fun readTodayData(): HealthData {
        val hc = client ?: return HealthData(null, null, null)

        val zone = ZoneId.of("Europe/Lisbon")
        val today = LocalDate.now(zone)
        val startOfDay = today.atStartOfDay(zone).toInstant()
        val now = java.time.Instant.now()

        val steps = try {
            val response = hc.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            response.records.sumOf { it.count }.toInt().takeIf { it > 0 }
        } catch (e: Exception) {
            null
        }

        // Sleep: read last night's sleep (yesterday evening to now)
        val sleepHours = try {
            val sleepStart = today.minusDays(1).atTime(18, 0).atZone(zone).toInstant()
            val response = hc.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(sleepStart, now)
                )
            )
            if (response.records.isNotEmpty()) {
                val totalMinutes = response.records.sumOf { record ->
                    java.time.Duration.between(record.startTime, record.endTime).toMinutes()
                }
                (totalMinutes / 60).toInt().takeIf { it > 0 }
            } else null
        } catch (e: Exception) {
            null
        }

        val waterGlasses = try {
            val response = hc.readRecords(
                ReadRecordsRequest(
                    recordType = HydrationRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            if (response.records.isNotEmpty()) {
                val totalLiters = response.records.sumOf {
                    it.volume.inLiters
                }
                // ~250ml per glass
                (totalLiters / 0.25).toInt().takeIf { it > 0 }
            } else null
        } catch (e: Exception) {
            null
        }

        return HealthData(steps, sleepHours, waterGlasses)
    }
}
