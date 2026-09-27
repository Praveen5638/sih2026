package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val productId: Long,
    val productName: String,
    val buyerId: String,
    val buyerName: String,
    val artisanName: String,
    val currentStatus: String = "ENQUIRING", // "ENQUIRING", "NEGOTIATING", "NEGOTIATION_CONFIRMED", "ORDER_READY"
    val agreedQuantity: Int = 0,
    val agreedUnitPrice: Double = 0.0,
    val buyerConfirmed: Boolean = false,
    val sellerConfirmed: Boolean = false,
    val lastMessageText: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
