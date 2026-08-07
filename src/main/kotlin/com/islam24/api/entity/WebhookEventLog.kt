package com.islam24.api.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID


@Entity
@Table(name = "webhook_event_logs")
class WebhookEventLog(
    @Id
    var eventId: String,
    @Column(nullable = false)
    var processedAt: Instant = Instant.now(),
)