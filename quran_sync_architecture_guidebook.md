# Quran Offline-First Sync Architecture Guidebook

This guidebook defines the end-to-end architecture for synchronizing Quran reading data (**Khatam Plans, Quran Bookmarks, and Recent Surahs**) between the **Islam24 Android Client** and **Spring Boot Backend (PostgreSQL)**.

---

## 1. Core Synchronization & Conflict Resolution Rules

### 1.1 The Soft Delete ➔ Hard Delete Lifecycle
To prevent deleted items from reappearing when syncing across multiple devices:
1. **Client Deletion (Soft Delete)**:
   When a bookmark, khatam, or recent surah is deleted in the Android UI:
   - Android sets `isDeleted = true, isSynced = false, updatedAt = System.currentTimeMillis()`.
2. **Sync Push**:
   Android sends the item with `isDeleted = true` to the backend.
3. **Backend Processing**:
   - The backend marks the entity `is_deleted = true` (or soft deletes it in PostgreSQL).
   - The backend includes the ID in the response ACK list (`confirmedDeletedBookmarkIds`, `confirmedDeletedKhatamIds`, `confirmedDeletedRecentSurahNumbers`).
4. **Client Cleanup (Permanent Hard Delete)**:
   When Android receives the response ACKs from the backend:
   ```sql
   DELETE FROM quran_bookmark WHERE id IN (:confirmedDeletedBookmarkIds)
   ```
   *This keeps the local Room database lean while ensuring deleted items never revive on other devices.*

---

### 1.2 Conflict Resolution Matrix

| Feature | Primary / Composite Key | Conflict Resolution Algorithm |
| :--- | :--- | :--- |
| **Khatam Plans (`khatam_plan`)** | `id: UUID` | **Max Global Ayah Wins (In-Progress)**:<br>• For `IN_PROGRESS` plans, the record with $\max(\text{lastReadGlobalAyahNumber})$ wins.<br>• If a device marked it `COMPLETED` or `ENDED`, the completed state wins if its timestamp is newer. |
| **Recent Surahs (`recent_surah`)** | `(user_id, surah_number)` | **Max Ayah Number Wins**:<br>• For the same Surah, $\max(\text{ayahNumber})$ wins.<br>• If deleted, the delete wins if `deleteTimestamp >= readTimestamp`. |
| **Quran Bookmarks (`quran_bookmark`)** | `id: UUID` | **Last-Write-Wins (LWW) with Tombstone**:<br>• If deleted on any device with a newer timestamp, delete wins.<br>• Otherwise, latest `updatedAt` wins. |

---

## 2. Backend Database Migration (`V12__create_quran_sync_tables.sql`)

```sql
-- 1. Khatam Quran Plans Table
CREATE TABLE khatam_plans (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL DEFAULT 'Khatam Quran',
    start_date_timestamp BIGINT NOT NULL,
    target_end_date_timestamp BIGINT NOT NULL,
    last_read_surah_number INT NOT NULL DEFAULT 1,
    last_read_ayah_number INT NOT NULL DEFAULT 1,
    last_read_global_ayah_number INT NOT NULL DEFAULT 1,
    completed_ayahs_count INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    completed_timestamp BIGINT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    updated_timestamp BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_khatam_plans_user_id ON khatam_plans(user_id);
CREATE INDEX idx_khatam_plans_user_updated ON khatam_plans(user_id, updated_timestamp);

-- 2. Quran Bookmarks Table
CREATE TABLE quran_bookmarks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    surah_number INT NOT NULL,
    ayah_number INT NOT NULL,
    global_ayah_number INT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now()
);
CREATE INDEX idx_quran_bookmarks_user_id ON quran_bookmarks(user_id);
CREATE INDEX idx_quran_bookmarks_user_updated ON quran_bookmarks(user_id, updated_at);

-- 3. Recent Surah Reading Position Table (Unique per user and surah)
CREATE TABLE recent_surahs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    surah_number INT NOT NULL,
    surah_name VARCHAR(100) NOT NULL,
    ayah_number INT NOT NULL,
    formatted_date VARCHAR(50) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    timestamp BIGINT NOT NULL,
    CONSTRAINT uq_user_recent_surah UNIQUE (user_id, surah_number)
);
CREATE INDEX idx_recent_surahs_user_id ON recent_surahs(user_id);
CREATE INDEX idx_recent_surahs_user_timestamp ON recent_surahs(user_id, timestamp);
```

---

## 3. Backend JPA Entities (`com.islam24.api.entity.quran`)

### 3.1 `KhatamPlanEntity.kt`
```kotlin
package com.islam24.api.entity.quran

import com.islam24.api.entity.User
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "khatam_plans")
class KhatamPlanEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var title: String = "Khatam Quran",

    @Column(nullable = false)
    var startDateTimestamp: Long,

    @Column(nullable = false)
    var targetEndDateTimestamp: Long,

    @Column(nullable = false)
    var lastReadSurahNumber: Int = 1,

    @Column(nullable = false)
    var lastReadAyahNumber: Int = 1,

    @Column(nullable = false)
    var lastReadGlobalAyahNumber: Int = 1,

    @Column(nullable = false)
    var completedAyahsCount: Int = 0,

    @Column(nullable = false)
    var status: String = "IN_PROGRESS",

    @Column(nullable = true)
    var completedTimestamp: Long? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

    @Column(nullable = false)
    var updatedTimestamp: Long = System.currentTimeMillis()
)
```

### 3.2 `QuranBookmarkEntity.kt`
```kotlin
package com.islam24.api.entity.quran

import com.islam24.api.entity.User
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "quran_bookmarks")
class QuranBookmarkEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var surahNumber: Int,

    @Column(nullable = false)
    var ayahNumber: Int,

    @Column(nullable = false)
    var globalAyahNumber: Int,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

    @Column(nullable = false)
    var updatedAt: Long = System.currentTimeMillis()
)
```

### 3.3 `RecentSurahEntity.kt`
```kotlin
package com.islam24.api.entity.quran

import com.islam24.api.entity.User
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "recent_surahs",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "surah_number"])]
)
class RecentSurahEntity(
    @Id
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var surahNumber: Int,

    @Column(nullable = false)
    var surahName: String,

    @Column(nullable = false)
    var ayahNumber: Int,

    @Column(nullable = false)
    var formattedDate: String,

    @Column(nullable = false)
    var isDeleted: Boolean = false,

    @Column(nullable = false)
    var timestamp: Long = System.currentTimeMillis()
)
```

---

## 4. Backend Repositories (`com.islam24.api.repository.quran`)

### 4.1 `KhatamPlanRepository.kt`
```kotlin
package com.islam24.api.repository.quran

import com.islam24.api.entity.quran.KhatamPlanEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface KhatamPlanRepository : JpaRepository<KhatamPlanEntity, UUID> {
    fun findByIdAndUserId(id: UUID, userId: UUID): KhatamPlanEntity?
    fun findByUserId(userId: UUID): List<KhatamPlanEntity>
    fun findByUserIdAndUpdatedTimestampAfter(userId: UUID, timestamp: Long): List<KhatamPlanEntity>
}
```

### 4.2 `QuranBookmarkRepository.kt`
```kotlin
package com.islam24.api.repository.quran

import com.islam24.api.entity.quran.QuranBookmarkEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface QuranBookmarkRepository : JpaRepository<QuranBookmarkEntity, UUID> {
    fun findByIdAndUserId(id: UUID, userId: UUID): QuranBookmarkEntity?
    fun findByUserId(userId: UUID): List<QuranBookmarkEntity>
    fun findByUserIdAndUpdatedAtAfter(userId: UUID, updatedAt: Long): List<QuranBookmarkEntity>
}
```

### 4.3 `RecentSurahRepository.kt`
```kotlin
package com.islam24.api.repository.quran

import com.islam24.api.entity.quran.RecentSurahEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RecentSurahRepository : JpaRepository<RecentSurahEntity, UUID> {
    fun findByUserIdAndSurahNumber(userId: UUID, surahNumber: Int): RecentSurahEntity?
    fun findByUserId(userId: UUID): List<RecentSurahEntity>
    fun findByUserIdAndTimestampAfter(userId: UUID, timestamp: Long): List<RecentSurahEntity>
}
```

---

## 5. Unified DTO Models (`com.islam24.api.dto.sync`)

### 5.1 Individual Quran DTOs

#### `KhatamPlanSyncDto.kt`
```kotlin
package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class KhatamPlanSyncDto(
    val id: UUID,
    val title: String = "Khatam Quran",
    @JsonProperty("startDateTimestamp") @JsonAlias("start_date_timestamp", "startDateTimestamp") val startDateTimestamp: Long,
    @JsonProperty("targetEndDateTimestamp") @JsonAlias("target_end_date_timestamp", "targetEndDateTimestamp") val targetEndDateTimestamp: Long,
    @JsonProperty("lastReadSurahNumber") @JsonAlias("last_read_surah_number", "lastReadSurahNumber") val lastReadSurahNumber: Int = 1,
    @JsonProperty("lastReadAyahNumber") @JsonAlias("last_read_ayah_number", "lastReadAyahNumber") val lastReadAyahNumber: Int = 1,
    @JsonProperty("lastReadGlobalAyahNumber") @JsonAlias("last_read_global_ayah_number", "lastReadGlobalAyahNumber") val lastReadGlobalAyahNumber: Int = 1,
    @JsonProperty("completedAyahsCount") @JsonAlias("completed_ayahs_count", "completedAyahsCount") val completedAyahsCount: Int = 0,
    val status: String = "IN_PROGRESS",
    @JsonProperty("completedTimestamp") @JsonAlias("completed_timestamp", "completedTimestamp") val completedTimestamp: Long? = null,
    @JsonProperty("isDeleted") @JsonAlias("is_deleted", "isDeleted") val isDeleted: Boolean = false,
    @JsonProperty("updatedTimestamp") @JsonAlias("updated_timestamp", "updatedTimestamp") val updatedTimestamp: Long = System.currentTimeMillis()
)
```

#### `QuranBookmarkSyncDto.kt`
```kotlin
package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class QuranBookmarkSyncDto(
    val id: UUID,
    @JsonProperty("surahNumber") @JsonAlias("surah_number", "surahNumber") val surahNumber: Int,
    @JsonProperty("ayahNumber") @JsonAlias("ayah_number", "ayahNumber") val ayahNumber: Int,
    @JsonProperty("globalAyahNumber") @JsonAlias("global_ayah_number", "globalAyahNumber") val globalAyahNumber: Int,
    @JsonProperty("isDeleted") @JsonAlias("is_deleted", "isDeleted") val isDeleted: Boolean = false,
    @JsonProperty("updatedAt") @JsonAlias("updated_at", "updatedAt") val updatedAt: Long = System.currentTimeMillis()
)
```

#### `RecentSurahSyncDto.kt`
```kotlin
package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class RecentSurahSyncDto(
    @JsonProperty("surahNumber") @JsonAlias("surah_number", "surahNumber") val surahNumber: Int,
    @JsonProperty("surahName") @JsonAlias("surah_name", "surahName") val surahName: String,
    @JsonProperty("ayahNumber") @JsonAlias("ayah_number", "ayahNumber") val ayahNumber: Int,
    @JsonProperty("formattedDate") @JsonAlias("formatted_date", "formattedDate") val formattedDate: String,
    @JsonProperty("isDeleted") @JsonAlias("is_deleted", "isDeleted") val isDeleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### 5.2 Updated `SyncRequestDto.kt` & `SyncResponseDto.kt`

#### `SyncRequestDto.kt`
```kotlin
package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.Instant

data class SyncRequestDto(
    @JsonAlias("last_synced_at", "lastSyncedAt")
    val lastSyncedAt: Instant? = null,

    // Prayer Sync
    @JsonAlias("prayer_settings", "prayerSettings", "prayerSettingSyncDto")
    val prayerSettings: PrayerSettingSyncDto? = null,
    @JsonAlias("prayer_logs", "prayerLogs", "prayerLogSyncDto")
    val prayerLogs: List<PrayerLogSyncDto> = emptyList(),

    // Quran Sync
    @JsonAlias("khatam_plans", "khatamPlans", "khatamPlanSyncDto")
    val khatamPlans: List<KhatamPlanSyncDto> = emptyList(),
    @JsonAlias("quran_bookmarks", "quranBookmarks", "quranBookmarkSyncDto")
    val quranBookmarks: List<QuranBookmarkSyncDto> = emptyList(),
    @JsonAlias("recent_surahs", "recentSurahs", "recentSurahSyncDto")
    val recentSurahs: List<RecentSurahSyncDto> = emptyList()
)
```

#### `SyncResponseDto.kt`
```kotlin
package com.islam24.api.dto.sync

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SyncResponseDto(
    @JsonAlias("synced_at", "syncedAt")
    val syncedAt: Instant,

    // Prayer Response
    val prayerSettings: PrayerSettingSyncDto? = null,
    val prayerLogs: List<PrayerLogSyncDto> = emptyList(),
    val syncedLogDates: List<LocalDate> = emptyList(),

    // Quran Remote Updates (for Android to merge)
    val khatamPlans: List<KhatamPlanSyncDto> = emptyList(),
    val quranBookmarks: List<QuranBookmarkSyncDto> = emptyList(),
    val recentSurahs: List<RecentSurahSyncDto> = emptyList(),

    // Explicit Deletion ACKs (for Android to permanently DELETE from Room)
    val confirmedDeletedKhatamIds: List<UUID> = emptyList(),
    val confirmedDeletedBookmarkIds: List<UUID> = emptyList(),
    val confirmedDeletedRecentSurahNumbers: List<Int> = emptyList()
)
```

---

## 6. Backend Merge Engine (`SyncService.kt`)

```kotlin
// ==========================================
// 1. KHATAM PLANS MERGE (Max Global Ayah Wins)
// ==========================================
private fun syncKhatamPlans(
    user: User, 
    incomingList: List<KhatamPlanSyncDto>
): List<UUID> {
    val confirmedDeleted = mutableListOf<UUID>()
    for (incoming in incomingList) {
        val existing = khatamPlanRepository.findByIdAndUserId(incoming.id, user.id)

        if (existing == null) {
            if (incoming.isDeleted) {
                confirmedDeleted.add(incoming.id)
            } else {
                khatamPlanRepository.save(incoming.toEntity(user = user))
            }
        } else {
            if (incoming.isDeleted) {
                existing.isDeleted = true
                existing.updatedTimestamp = maxOf(existing.updatedTimestamp, incoming.updatedTimestamp)
                khatamPlanRepository.save(existing)
                confirmedDeleted.add(incoming.id)
            } else {
                // Rule: Max Global Ayah Wins for In-Progress
                if (incoming.lastReadGlobalAyahNumber > existing.lastReadGlobalAyahNumber) {
                    existing.lastReadGlobalAyahNumber = incoming.lastReadGlobalAyahNumber
                    existing.lastReadSurahNumber = incoming.lastReadSurahNumber
                    existing.lastReadAyahNumber = incoming.lastReadAyahNumber
                    existing.completedAyahsCount = incoming.completedAyahsCount
                }
                if (incoming.status == "COMPLETED") {
                    existing.status = "COMPLETED"
                    existing.completedTimestamp = incoming.completedTimestamp
                }
                existing.updatedTimestamp = maxOf(existing.updatedTimestamp, incoming.updatedTimestamp)
                existing.isDeleted = false
                khatamPlanRepository.save(existing)
            }
        }
    }
    return confirmedDeleted
}

// ==========================================
// 2. RECENT SURAHS MERGE (Max Ayah Number Wins)
// ==========================================
private fun syncRecentSurahs(
    user: User, 
    incomingList: List<RecentSurahSyncDto>
): List<Int> {
    val confirmedDeleted = mutableListOf<Int>()
    for (incoming in incomingList) {
        val existing = recentSurahRepository.findByUserIdAndSurahNumber(user.id, incoming.surahNumber)

        if (existing == null) {
            if (incoming.isDeleted) {
                confirmedDeleted.add(incoming.surahNumber)
            } else {
                recentSurahRepository.save(incoming.toEntity(user = user))
            }
        } else {
            if (incoming.isDeleted && incoming.timestamp >= existing.timestamp) {
                existing.isDeleted = true
                existing.timestamp = incoming.timestamp
                recentSurahRepository.save(existing)
                confirmedDeleted.add(incoming.surahNumber)
            } else {
                // Rule: Max Ayah Number Wins
                if (incoming.ayahNumber > existing.ayahNumber) {
                    existing.ayahNumber = incoming.ayahNumber
                    existing.formattedDate = incoming.formattedDate
                }
                existing.timestamp = maxOf(existing.timestamp, incoming.timestamp)
                existing.isDeleted = false
                recentSurahRepository.save(existing)
            }
        }
    }
    return confirmedDeleted
}

// ==========================================
// 3. BOOKMARKS MERGE (LWW + Tombstone)
// ==========================================
private fun syncBookmarks(
    user: User, 
    incomingList: List<QuranBookmarkSyncDto>
): List<UUID> {
    val confirmedDeleted = mutableListOf<UUID>()
    for (incoming in incomingList) {
        val existing = quranBookmarkRepository.findByIdAndUserId(incoming.id, user.id)

        if (existing == null) {
            if (incoming.isDeleted) {
                confirmedDeleted.add(incoming.id)
            } else {
                quranBookmarkRepository.save(incoming.toEntity(user = user))
            }
        } else {
            if (incoming.isDeleted) {
                existing.isDeleted = true
                existing.updatedAt = maxOf(existing.updatedAt, incoming.updatedAt)
                quranBookmarkRepository.save(existing)
                confirmedDeleted.add(incoming.id)
            } else if (incoming.updatedAt > existing.updatedAt) {
                existing.surahNumber = incoming.surahNumber
                existing.ayahNumber = incoming.ayahNumber
                existing.globalAyahNumber = incoming.globalAyahNumber
                existing.updatedAt = incoming.updatedAt
                existing.isDeleted = false
                quranBookmarkRepository.save(existing)
            }
        }
    }
    return confirmedDeleted
}
```

---

## 7. Android Client Sync Lifecycle (`SyncRepositoryImpl.kt`)

```kotlin
override suspend fun performFullSync(): Boolean {
    // 1. Gather all unsynced local data (where isSynced = false)
    val lastSyncedAt = syncDataStore.getLastSyncedAt()
    val unsyncedLogs = prayerLogDao.getUnsyncedLogs()
    val unsyncedKhatams = khatamPlanDao.getUnsyncedPlans()
    val unsyncedBookmarks = quranBookmarkDao.getUnsyncedBookmarks()
    val unsyncedRecent = recentSurahDao.getUnsyncedRecent()

    val request = SyncRequestDto(
        lastSyncedAt = lastSyncedAt,
        prayerLogs = unsyncedLogs.map { it.toDto() },
        khatamPlans = unsyncedKhatams.map { it.toDto() },
        quranBookmarks = unsyncedBookmarks.map { it.toDto() },
        recentSurahs = unsyncedRecent.map { it.toDto() }
    )

    // 2. Call unified sync endpoint
    val response = syncApi.syncData(request) ?: return false

    // 3. HARD DELETE confirmed deleted records from Room
    if (response.confirmedDeletedBookmarkIds.isNotEmpty()) {
        quranBookmarkDao.hardDeleteByIds(response.confirmedDeletedBookmarkIds.map { it.toString() })
    }
    if (response.confirmedDeletedKhatamIds.isNotEmpty()) {
        khatamPlanDao.hardDeleteByIds(response.confirmedDeletedKhatamIds.map { it.toString() })
    }
    if (response.confirmedDeletedRecentSurahNumbers.isNotEmpty()) {
        recentSurahDao.hardDeleteBySurahNumbers(response.confirmedDeletedRecentSurahNumbers)
    }

    // 4. Mark pushed items as synced
    if (response.syncedLogDates.isNotEmpty()) {
        prayerLogDao.markAsSynced(response.syncedLogDates)
    }
    if (unsyncedBookmarks.isNotEmpty()) {
        quranBookmarkDao.markAsSynced(unsyncedBookmarks.filter { !it.isDeleted }.map { it.id })
    }
    if (unsyncedKhatams.isNotEmpty()) {
        khatamPlanDao.markAsSynced(unsyncedKhatams.filter { !it.isDeleted }.map { it.id })
    }
    if (unsyncedRecent.isNotEmpty()) {
        recentSurahDao.markAsSynced(unsyncedRecent.filter { !it.isDeleted }.map { it.surahNumber })
    }

    // 5. Merge incoming server updates into Room
    // Khatams: Max Global Ayah Wins
    for (serverPlan in response.khatamPlans) {
        val local = khatamPlanDao.getById(serverPlan.id.toString())
        if (local == null) {
            khatamPlanDao.upsert(serverPlan.toEntity(isSynced = true))
        } else {
            val maxGlobal = maxOf(local.lastReadGlobalAyahNumber, serverPlan.lastReadGlobalAyahNumber)
            val winningSurah = if (serverPlan.lastReadGlobalAyahNumber >= local.lastReadGlobalAyahNumber) serverPlan.lastReadSurahNumber else local.lastReadSurahNumber
            val winningAyah = if (serverPlan.lastReadGlobalAyahNumber >= local.lastReadGlobalAyahNumber) serverPlan.lastReadAyahNumber else local.lastReadAyahNumber
            khatamPlanDao.upsert(local.copy(
                lastReadGlobalAyahNumber = maxGlobal,
                lastReadSurahNumber = winningSurah,
                lastReadAyahNumber = winningAyah,
                completedAyahsCount = maxOf(local.completedAyahsCount, serverPlan.completedAyahsCount),
                isSynced = true
            ))
        }
    }

    // Recent Surahs: Max Ayah Wins
    for (serverRecent in response.recentSurahs) {
        val local = recentSurahDao.getBySurahNumber(serverRecent.surahNumber)
        if (local == null) {
            recentSurahDao.upsert(serverRecent.toEntity(isSynced = true))
        } else {
            recentSurahDao.upsert(local.copy(
                ayahNumber = maxOf(local.ayahNumber, serverRecent.ayahNumber),
                isSynced = true
            ))
        }
    }

    // Bookmarks: Upsert remote active bookmarks
    for (serverBookmark in response.quranBookmarks) {
        quranBookmarkDao.upsert(serverBookmark.toEntity(isSynced = true))
    }

    // 6. Persist new server sync timestamp
    syncDataStore.setLastSyncedAt(response.syncedAt)
    return true
}
```
