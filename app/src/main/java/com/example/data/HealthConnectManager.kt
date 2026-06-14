package com.example.data

import android.content.Context
import android.os.Build
import android.util.Log

class HealthConnectManager(private val context: Context) {

    // Action to request permissions in Android 14+
    val requestPermissionsAction: String
        get() = "android.health.connect.action.REQUEST_PERMISSIONS"

    // Set of permissions we need
    val requiredPermissions: Set<String>
        get() = setOf(
            "android.permission.health.READ_SLEEP",
            "android.permission.health.READ_HEART_RATE"
        )

    /**
     * Checks if Health Connect is available on this device and API is available.
     */
    fun isAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val manager = context.getSystemService("healthconnect")
            manager != null
        } else {
            false
        }
    }

    /**
     * Check if all required Read permissions are granted.
     */
    fun hasPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return requiredPermissions.all { perm ->
                context.checkSelfPermission(perm) == android.content.pm.PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }

    /**
     * Fetches sleep sessions from the past 7 days.
     * Returns a list of SleepRecords containing duration in minutes and starting time.
     */
    suspend fun fetchSleepDataPast7Days(): List<SleepRecordDto> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return getFallbackSleepData()
        }

        if (!hasPermissions()) {
            return getFallbackSleepData()
        }

        return try {
            val data = HealthConnectApiHelper.fetchSleepData(context)
            if (data.isEmpty()) {
                getFallbackSleepData()
            } else {
                data
            }
        } catch (e: Throwable) {
            Log.e("HealthConnectManager", "Exception in fetchSleepDataPast7Days", e)
            getFallbackSleepData()
        }
    }

    // Default mock fallback sleep logs in case permission is not granted or empty
    fun getFallbackSleepData(): List<SleepRecordDto> {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        return listOf(
            SleepRecordDto(now - 6 * oneDayMs, 460, "Deep sleep"),
            SleepRecordDto(now - 5 * oneDayMs, 420, "Mild sleep"),
            SleepRecordDto(now - 4 * oneDayMs, 510, "Great recovery"),
            SleepRecordDto(now - 3 * oneDayMs, 380, "Slightly interrupted"),
            SleepRecordDto(now - 2 * oneDayMs, 450, "Consistent cycle"),
            SleepRecordDto(now - 1 * oneDayMs, 490, "Restful night"),
            SleepRecordDto(now, 480, "Last Night's Sleep")
        )
    }
}

/**
 * Isolated Helper object containing API 34 specific references.
 * This helper is only loaded on JVM if runtime execution reaches it on API >= 34.
 */
@androidx.annotation.RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
private object HealthConnectApiHelper {
    suspend fun fetchSleepData(context: Context): List<SleepRecordDto> {
        return try {
            val hcm = context.getSystemService("healthconnect") as? android.health.connect.HealthConnectManager
                ?: return emptyList()

            val endTime = java.time.Instant.now()
            val startTime = endTime.minus(7, java.time.temporal.ChronoUnit.DAYS)

            val timeInstantRangeFilter = android.health.connect.TimeInstantRangeFilter.Builder()
                .setStartTime(startTime)
                .setEndTime(endTime)
                .build()

            val request = android.health.connect.ReadRecordsRequestUsingFilters.Builder(
                android.health.connect.datatypes.SleepSessionRecord::class.java
            )
                .setTimeRangeFilter(timeInstantRangeFilter)
                .build()

            val executor = java.util.concurrent.Executors.newSingleThreadExecutor()

            kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
                val outcomeReceiver = object : android.os.OutcomeReceiver<
                    android.health.connect.ReadRecordsResponse<android.health.connect.datatypes.SleepSessionRecord>,
                    android.health.connect.HealthConnectException
                > {
                    override fun onResult(result: android.health.connect.ReadRecordsResponse<android.health.connect.datatypes.SleepSessionRecord>) {
                        val records = result.records
                        val list = records.map { record ->
                            val durationMinutes = java.time.Duration.between(record.startTime, record.endTime).toMinutes().toInt()
                            SleepRecordDto(
                                timestamp = record.startTime.toEpochMilli(),
                                durationMinutes = durationMinutes,
                                notes = record.notes?.toString() ?: "Sleep Session"
                            )
                        }
                        if (continuation.isActive) {
                            continuation.resume(list, null)
                        }
                    }

                    override fun onError(error: android.health.connect.HealthConnectException) {
                        Log.e("HealthConnectApiHelper", "Error querying sleep records", error)
                        if (continuation.isActive) {
                            continuation.resume(emptyList(), null)
                        }
                    }
                }
                
                hcm.readRecords(request, executor, outcomeReceiver)
            }
        } catch (e: Throwable) {
            Log.e("HealthConnectApiHelper", "Error in HealthConnectApiHelper fetchSleepData", e)
            emptyList()
        }
    }
}

data class SleepRecordDto(
    val timestamp: Long,
    val durationMinutes: Int,
    val notes: String
)
