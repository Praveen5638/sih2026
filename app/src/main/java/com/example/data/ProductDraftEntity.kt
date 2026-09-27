package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_drafts")
data class ProductDraftEntity(
    @PrimaryKey val draftId: String = "ACTIVE_DRAFT",
    val currentStepName: String = "IMAGE_STUDIO",
    val productName: String = "",
    val category: String = "",
    val craft: String = "",
    val material: String = "",
    val technique: String = "",
    val color: String = "",
    val dimensions: String = "",
    val productionTime: String = "",
    val descriptionHi: String = "",
    val descriptionEn: String = "",
    val seoTags: String = "",
    val transcript: String = "",
    val originalImageUrl: String = "",
    val enhancedImageUrl: String = "",
    val userSelectedPhotoChoice: String = "ENHANCED",
    val materialCost: Double = 800.0,
    val labourCost: Double = 400.0,
    val otherCost: Double = 120.0,
    val costFloor: Double = 1320.0,
    val recommendedPrice: Double = 1750.0,
    val sellingPrice: Double = 1750.0,
    val isAiProcessedOffline: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
