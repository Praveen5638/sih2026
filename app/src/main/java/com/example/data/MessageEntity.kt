package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val clientMessageId: String, // Idempotency key
    val senderId: String,
    val senderType: String, // "BUYER" or "SELLER"
    val text: String,
    val language: String = "hi",
    val messageType: String = "TEXT", // "TEXT", "VOICE_TRANSCRIPT", "OFFER", "CONFIRMATION"
    val extractedQuantity: Int? = null,
    val extractedPrice: Double? = null,
    val status: String = "SENT", // "PENDING", "QUEUED_OFFLINE", "SENT", "DELIVERED", "READ"
    val createdAt: Long = System.currentTimeMillis()
)
