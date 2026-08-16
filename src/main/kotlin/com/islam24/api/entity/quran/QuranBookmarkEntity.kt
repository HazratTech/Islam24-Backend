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