package com.example.ai

data class ProductCatalogResult(
    val productName: String = "",
    val category: String = "",
    val craft: String = "",
    val material: String = "",
    val color: String = "",
    val technique: String = "",
    val dimensions: String = "Not specified",
    val productionTime: String = "Not specified",
    val productStory: String = "",
    val careInstructions: String = "Hand wash gently with mild detergent.",
    val descriptionHi: String = "",
    val descriptionEn: String = "",
    val seoTags: String = "",
    val missingFields: List<String> = emptyList(),
    val uncertainFields: List<String> = emptyList(),
    val confidenceByField: Map<String, Float> = emptyMap()
)
