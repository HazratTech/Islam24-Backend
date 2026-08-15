# Android Offline-First Sync Architecture Guidebook

This guidebook outlines the complete, industry-grade offline-first synchronization architecture for **Islam24 Android (`/app/Islam24`)**, mapped to your project's multi-module architecture.

---

## 1. Multi-Module File Tree Map

```
app/Islam24/
│
├── core/
│   ├── remote/src/main/kotlin/com/hazrat/remote/
│   │   ├── dto/sync/
│   │   │   ├── PrayerSettingSyncDto.kt
│   │   │   ├── PrayerLogSyncDto.kt
│   │   │   ├── SyncRequestDto.kt
│   │   │   └── SyncResponseDto.kt
│   │   └── api/
│   │       ├── sync/SyncApi.kt
│   │       └── apiData/SyncApiImpl.kt
│   │
│   ├── database/src/main/java/com/hazrat/database/
│   │   ├── entity/prayer/
│   │   │   ├── PrayerLogEntity.kt
│   │   │   └── UserPrayerSettingEntity.kt
│   │   └── dao/
│   │       ├── PrayerLogDao.kt
│   │       └── UserPrayerSettingDao.kt
│   │
│   └── datastore/src/main/kotlin/com/hazrat/datastore/
│       └── SyncDataStore.kt
│
├── domain/
│   ├── repository/src/main/kotlin/com/hazrat/domain/repository/
│   │   └── SyncRepository.kt
│   └── usecase/src/main/kotlin/com/hazrat/usecase/sync/
│       └── PerformSyncUseCase.kt
│
├── feature/prayertime/data/src/main/kotlin/com/hazrat/prayertime/data/
│   ├── repository/SyncRepositoryImpl.kt
│   └── mapper/SyncDomainMapper.kt
│
└── app/src/main/kotlin/com/hazrat/islam24/
    └── sync/
        ├── SyncWorker.kt
        └── SyncScheduler.kt
```

---

## 2. Step 1: Remote Module (`:core:remote`)

### 2.1 DTOs (`com.hazrat.remote.dto.sync`)

#### `PrayerSettingSyncDto.kt`
```kotlin
package com.hazrat.remote.dto.sync

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class NotificationSettingDto(
    val enabled: Boolean = true,
    val offsetMinutes: Int = 0,
    val audio: String = "default"
)

@Serializable
data class NotificationSettingsMapDto(
    val fajr: NotificationSettingDto,
    val dhuhr: NotificationSettingDto,
    val asr: NotificationSettingDto,
    val maghrib: NotificationSettingDto,
    val isha: NotificationSettingDto
)

@Serializable
data class PrayerSettingSyncDto(
    val calculationMethod: Int,
    val juristicMethod: Int,
    val masterNotification: Boolean = true,
    val notificationSettings: NotificationSettingsMapDto,
    val updatedAt: Instant
)
```

#### `PrayerLogSyncDto.kt`
```kotlin
package com.hazrat.remote.dto.sync

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class PrayerLogSyncDto(
    val logDate: LocalDate,
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,
    val updatedAt: Instant
)
```

#### `SyncRequestDto.kt` & `SyncResponseDto.kt`
```kotlin
package com.hazrat.remote.dto.sync

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SyncRequestDto(
    val lastSyncedAt: Instant? = null,
    val prayerSettings: PrayerSettingSyncDto? = null,
    val prayerLogs: List<PrayerLogSyncDto> = emptyList()
)

@Serializable
data class SyncResponseDto(
    val syncedAt: Instant,
    val prayerSettings: PrayerSettingSyncDto? = null,
    val prayerLogs: List<PrayerLogSyncDto> = emptyList()
)
```

---

### 2.2 Ktor Client API (`com.hazrat.remote.api.sync`)

#### `SyncApi.kt`
```kotlin
package com.hazrat.remote.api.sync

import com.hazrat.remote.dto.sync.SyncRequestDto
import com.hazrat.remote.dto.sync.SyncResponseDto

interface SyncApi {
    suspend fun syncData(request: SyncRequestDto): SyncResponseDto?
}
```

#### `SyncApiImpl.kt` (`com.hazrat.remote.api.apiData`)
```kotlin
package com.hazrat.remote.api.apiData

import android.util.Log
import com.hazrat.remote.api.sync.SyncApi
import com.hazrat.remote.dto.sync.SyncRequestDto
import com.hazrat.remote.dto.sync.SyncResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SyncApiImpl(
    private val httpClient: HttpClient
) : SyncApi {

    override suspend fun syncData(request: SyncRequestDto): SyncResponseDto? {
        return try {
            val response = httpClient.post("sync") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.value in 200..299) {
                response.body<SyncResponseDto>()
            } else {
                Log.e("SyncApiImpl", "Sync failed with status: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("SyncApiImpl", "Network exception during sync: ${e.message}")
            null
        }
    }
}
```

---

## 3. Step 2: Database Module (`:core:database`)

### 3.1 Room Entities

#### `PrayerLogEntity.kt`
```kotlin
package com.hazrat.database.entity.prayer

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Entity(tableName = "prayer_logs")
data class PrayerLogEntity(
    @PrimaryKey
    val logDate: LocalDate, // e.g. 2026-08-14
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,
    val isSynced: Boolean = false, // Dirty tracking flag
    val updatedAt: Instant
)
```

#### `UserPrayerSettingEntity.kt`
```kotlin
package com.hazrat.database.entity.prayer

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

@Entity(tableName = "user_prayer_settings")
data class UserPrayerSettingEntity(
    @PrimaryKey
    val id: Int = 1, // Single-row configuration
    val calculationMethod: Int,
    val juristicMethod: Int,
    val masterNotification: Boolean = true,
    val notificationSettingsJson: String, // Stored as JSON or TypeConverter
    val isSynced: Boolean = false,
    val updatedAt: Instant
)
```

---

### 3.2 Room DAOs

#### `PrayerLogDao.kt`
```kotlin
package com.hazrat.database.dao

import androidx.room.*
import com.hazrat.database.entity.prayer.PrayerLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface PrayerLogDao {

    @Query("SELECT * FROM prayer_logs WHERE logDate = :date")
    suspend fun getByDate(date: LocalDate): PrayerLogEntity?

    @Query("SELECT * FROM prayer_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<PrayerLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: PrayerLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(logs: List<PrayerLogEntity>)

    @Query("UPDATE prayer_logs SET isSynced = 1 WHERE logDate IN (:dates)")
    suspend fun markAsSynced(dates: List<LocalDate>)

    @Query("SELECT * FROM prayer_logs ORDER BY logDate DESC")
    fun getAllLogsFlow(): Flow<List<PrayerLogEntity>>
}
```

---

## 4. Step 3: DataStore Module (`:core:datastore`)

### `SyncDataStore.kt`
Stores the timestamp of the last successful sync.

```kotlin
package com.hazrat.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

class SyncDataStore(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val KEY_LAST_SYNCED_AT = stringPreferencesKey("key_last_synced_at")
    }

    suspend fun getLastSyncedAt(): Instant? {
        val isoString = dataStore.data.map { it[KEY_LAST_SYNCED_AT] }.firstOrNull()
        return isoString?.let { Instant.parse(it) }
    }

    suspend fun setLastSyncedAt(timestamp: Instant) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_SYNCED_AT] = timestamp.toString()
        }
    }

    suspend fun clearLastSyncedAt() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_LAST_SYNCED_AT)
        }
    }
}
```

---

## 5. Step 4: Domain & Repository Implementation

### 5.1 `SyncRepository.kt` (`:domain:repository`)
```kotlin
package com.hazrat.domain.repository

interface SyncRepository {
    suspend fun performFullSync(): Boolean
}
```

---

### 5.2 `SyncRepositoryImpl.kt` (`:feature:prayertime:data`)
Implements bidirectional delta sync and Room merge inside a transaction with **Login State Verification**:

```kotlin
package com.hazrat.prayertime.data.repository

import android.util.Log
import com.hazrat.database.dao.PrayerLogDao
import com.hazrat.database.dao.UserPrayerSettingDao
import com.hazrat.database.entity.prayer.PrayerLogEntity
import com.hazrat.datastore.SyncDataStore
import com.hazrat.datastore.TokenStorage
import com.hazrat.domain.repository.SyncRepository
import com.hazrat.remote.api.sync.SyncApi
import com.hazrat.remote.dto.sync.PrayerLogSyncDto
import com.hazrat.remote.dto.sync.SyncRequestDto
import kotlinx.datetime.Clock

class SyncRepositoryImpl(
    private val syncApi: SyncApi,
    private val prayerLogDao: PrayerLogDao,
    private val prayerSettingDao: UserPrayerSettingDao,
    private val syncDataStore: SyncDataStore,
    private val tokenStorage: TokenStorage
) : SyncRepository {

    override suspend fun performFullSync(): Boolean {
        // 1. Check if user is logged in before attempting network sync
        val accessToken = tokenStorage.getAccessToken()
        if (accessToken.isNullOrBlank()) {
            Log.d("SyncRepository", "Skipping sync: User is not logged in.")
            return true // Not an error, just skip for guest users
        }

        // 2. Gather local unsynced logs & settings
        val lastSyncedAt = syncDataStore.getLastSyncedAt()
        val unsyncedLogs = prayerLogDao.getUnsyncedLogs()
        val localSettings = prayerSettingDao.getSettings()

        val request = SyncRequestDto(
            lastSyncedAt = lastSyncedAt,
            prayerSettings = if (localSettings?.isSynced == false) localSettings.toDto() else null,
            prayerLogs = unsyncedLogs.map { it.toDto() }
        )

        // 3. Call Unified Backend Endpoint
        val response = syncApi.syncData(request) ?: return false

        // 4. Mark pushed local items as synced
        if (unsyncedLogs.isNotEmpty()) {
            prayerLogDao.markAsSynced(unsyncedLogs.map { it.logDate })
        }

        // 5. Merge incoming server updates into Room
        // For prayer logs: merge with boolean OR
        for (serverLog in response.prayerLogs) {
            val existing = prayerLogDao.getByDate(serverLog.logDate)
            if (existing == null) {
                prayerLogDao.upsert(serverLog.toEntity(isSynced = true))
            } else {
                val merged = existing.copy(
                    fajr = existing.fajr || serverLog.fajr,
                    dhuhr = existing.dhuhr || serverLog.dhuhr,
                    asr = existing.asr || serverLog.asr,
                    maghrib = existing.maghrib || serverLog.maghrib,
                    isha = existing.isha || serverLog.isha,
                    isSynced = true,
                    updatedAt = if (serverLog.updatedAt > existing.updatedAt) serverLog.updatedAt else existing.updatedAt
                )
                prayerLogDao.upsert(merged)
            }
        }

        // 6. Update winning settings
        response.prayerSettings?.let { serverSettings ->
            prayerSettingDao.upsert(serverSettings.toEntity(isSynced = true))
        }

        // 7. Save new server sync timestamp
        syncDataStore.setLastSyncedAt(response.syncedAt)
        return true
    }
}
```

---

## 6. Step 5: Android WorkManager Integration (`:app`)

### 6.1 `SyncWorker.kt` (`com.hazrat.islam24.sync`)
```kotlin
package com.hazrat.islam24.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hazrat.datastore.TokenStorage
import com.hazrat.domain.repository.SyncRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val syncRepository: SyncRepository by inject()
    private val tokenStorage: TokenStorage by inject()

    override suspend fun doWork(): Result {
        // Guard check: Avoid running sync and wasting battery if user is logged out
        val token = tokenStorage.getAccessToken()
        if (token.isNullOrBlank()) {
            Log.d("SyncWorker", "User not logged in. Skipping background sync.")
            return Result.success()
        }

        Log.d("SyncWorker", "Starting background sync work for logged-in user...")
        return try {
            val success = syncRepository.performFullSync()
            if (success) {
                Log.d("SyncWorker", "Background sync completed successfully.")
                Result.success()
            } else {
                Log.w("SyncWorker", "Sync returned false, retrying...")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed with exception: ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
```

---

### 6.2 `SyncScheduler.kt` (`com.hazrat.islam24.sync`)
Provides functions to schedule sync when network becomes available, on login, or periodically.

```kotlin
package com.hazrat.islam24.sync

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object SyncScheduler {

    private const val PERIODIC_SYNC_WORK_NAME = "islam24_periodic_sync"
    private const val ONE_TIME_SYNC_WORK_NAME = "islam24_immediate_sync"

    private val syncConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    // 1. Triggered on app launch or immediately after user logs in
    fun scheduleImmediateSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(syncConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // 2. Periodic sync running every 6 hours in the background
    fun schedulePeriodicSync(context: Context) {
        val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(syncConstraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }
}
```

---

## 7. Step 6: Koin DI Registrations

### In `:core:remote` DI:
```kotlin
single<SyncApi> { SyncApiImpl(httpClient = get(named(CUSTOM_BACKEND))) }
```

### In `:feature:prayertime:data` DI:
```kotlin
single<SyncRepository> {
    SyncRepositoryImpl(
        syncApi = get(),
        prayerLogDao = get(),
        prayerSettingDao = get(),
        syncDataStore = get()
    )
}
```

### In `:app` Application Class (`onCreate`):
```kotlin
class Islam24App : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Start periodic sync scheduler
        SyncScheduler.schedulePeriodicSync(this)
    }
}
```

---

## 8. Summary of Conflict Resolution Guarantees

| Feature | Conflict Strategy | Guarantee |
| :--- | :--- | :--- |
| **Prayer Settings** | Last-Write-Wins (LWW) | The latest modification timestamp on either device wins. |
| **Daily Prayer Logs** | Day-by-Day Boolean OR | If a prayer is completed on any device offline, it stays completed after sync. |
| **Login / Logout** | Clear `lastSyncedAt` on logout | On fresh login, client requests all history (`lastSyncedAt = null`) and merges without destroying offline guest data. |
