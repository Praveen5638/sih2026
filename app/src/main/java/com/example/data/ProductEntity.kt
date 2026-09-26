package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val category: String,
    val craft: String,
    val material: String,
    val technique: String,
    val color: String,
    val dimensions: String,
    val productionTime: String,
    val descriptionHi: String,
    val descriptionEn: String,
    val seoTags: String,
    val originalImageUrl: String,
    val enhancedImageUrl: String,
    val materialCost: Double,
    val labourCost: Double,
    val otherCost: Double,
    val costFloor: Double,
    val recommendedPrice: Double,
    val sellingPrice: Double,
    val status: String, // "Draft", "Ready", "Published"
    val createdAt: Long = System.currentTimeMillis()
)
