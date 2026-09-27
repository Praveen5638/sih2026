package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseProductDto(
    @Json(name = "local_id") val localId: Long,
    @Json(name = "product_name") val productName: String,
    @Json(name = "category") val category: String,
    @Json(name = "craft") val craft: String,
    @Json(name = "material") val material: String,
    @Json(name = "technique") val technique: String,
    @Json(name = "color") val color: String,
    @Json(name = "dimensions") val dimensions: String = "Standard",
    @Json(name = "production_time") val productionTime: String = "2 Days",
    @Json(name = "description_hi") val descriptionHi: String = "",
    @Json(name = "description_en") val descriptionEn: String = "",
    @Json(name = "seo_tags") val seoTags: String = "",
    @Json(name = "original_image_url") val originalImageUrl: String = "",
    @Json(name = "enhanced_image_url") val enhancedImageUrl: String = "",
    @Json(name = "material_cost") val materialCost: Double = 0.0,
    @Json(name = "labour_cost") val labourCost: Double = 0.0,
    @Json(name = "other_cost") val otherCost: Double = 0.0,
    @Json(name = "cost_floor") val costFloor: Double = 0.0,
    @Json(name = "recommended_price") val recommendedPrice: Double = 0.0,
    @Json(name = "selling_price") val sellingPrice: Double = 0.0,
    @Json(name = "status") val status: String = "Ready"
)

@JsonClass(generateAdapter = true)
data class SupabaseConversationDto(
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "product_id") val productId: Long,
    @Json(name = "product_name") val productName: String,
    @Json(name = "buyer_id") val buyerId: String,
    @Json(name = "buyer_name") val buyerName: String,
    @Json(name = "artisan_name") val artisanName: String,
    @Json(name = "current_status") val currentStatus: String = "ENQUIRING",
    @Json(name = "agreed_quantity") val agreedQuantity: Int = 1,
    @Json(name = "agreed_unit_price") val agreedUnitPrice: Double = 0.0,
    @Json(name = "buyer_confirmed") val buyerConfirmed: Boolean = false,
    @Json(name = "seller_confirmed") val sellerConfirmed: Boolean = false,
    @Json(name = "last_message_text") val lastMessageText: String = ""
)

@JsonClass(generateAdapter = true)
data class SupabaseMessageDto(
    @Json(name = "message_id") val messageId: String,
    @Json(name = "conversation_id") val conversationId: String,
    @Json(name = "client_message_id") val clientMessageId: String,
    @Json(name = "sender_id") val senderId: String,
    @Json(name = "sender_type") val senderType: String,
    @Json(name = "text") val text: String,
    @Json(name = "language") val language: String = "hi",
    @Json(name = "message_type") val messageType: String = "TEXT",
    @Json(name = "extracted_quantity") val extractedQuantity: Int? = null,
    @Json(name = "extracted_price") val extractedPrice: Double? = null,
    @Json(name = "status") val status: String = "SENT"
)
