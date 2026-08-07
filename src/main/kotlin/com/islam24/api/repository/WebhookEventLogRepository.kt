package com.islam24.api.repository

import com.islam24.api.entity.WebhookEventLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WebhookEventLogRepository : JpaRepository<WebhookEventLog, String> {

}