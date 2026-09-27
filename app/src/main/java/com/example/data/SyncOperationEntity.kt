package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sync_operations")
data class SyncOperationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val clientOperationId: String, // Idempotency key
    val operationType: String, // "CREATE_PRODUCT", "UPLOAD_IMAGE", "SYNC_PRICING"
    val entityId: String,
    val payloadJson: String,
    val status: String = "PENDING", // "PENDING", "SYNCING", "SYNCED", "FAILED_RETRYABLE", "FAILED_PERMANENT"
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAttemptAt: Long = 0L,
    val errorMessage: String? = null
)
