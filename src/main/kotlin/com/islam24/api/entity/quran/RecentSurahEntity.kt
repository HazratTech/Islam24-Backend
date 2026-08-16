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
