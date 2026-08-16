package com.islam24.api.entity.quran

import com.islam24.api.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
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