package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProductListingResult(
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
    val isGeneratedOffline: Boolean = false
)

data class PricingResult(
    val costFloor: Double,
    val recommendedMin: Double,
    val recommendedPrice: Double,
    val recommendedMax: Double,
    val reasoning: String
)

object GeminiAiHelper {

    suspend fun generateCatalogFromVoice(transcript: String, craftType: String): ProductListingResult = withContext(Dispatchers.IO) {
        val isOnline = com.example.network.NetworkMonitor.isOnline()
        kotlinx.coroutines.delay(400) // Fast local response
        val cleanDesc = transcript.ifBlank { "Handcrafted artisan product created with dedication." }

        if (!isOnline) {
            ProductListingResult(
                productName = "Handcrafted $craftType",
                category = "Traditional Handicraft",
                craft = craftType,
                material = "Authentic Craft Material",
                technique = "Handmade",
                color = "Natural",
                dimensions = "Standard Size",
                productionTime = "3 Days",
                descriptionHi = "यह $craftType कारीगरों द्वारा बनाई गई एक उत्कृष्ट हस्तनिर्मित वस्तु है। (ऑफ़लाइन सहेजा गया) $cleanDesc",
                descriptionEn = "An exquisite $craftType creation handcrafted by skilled artisans. (Saved Offline) $cleanDesc",
                seoTags = "Handmade $craftType, Traditional Craft, Offline Saved",
                isGeneratedOffline = true
            )
        } else {
            ProductListingResult(
                productName = "Handcrafted $craftType Masterpiece",
                category = "Traditional Craft",
                craft = craftType,
                material = "Authentic Local Material",
                technique = "Handmade / Handloom",
                color = "Natural Rich Tones",
                dimensions = "Standard Size",
                productionTime = "3 Days",
                descriptionHi = "यह $craftType कारीगरों द्वारा बनाई गई एक उत्कृष्ट हस्तनिर्मित वस्तु है। $cleanDesc",
                descriptionEn = "An exquisite $craftType creation handcrafted by skilled artisans. $cleanDesc",
                seoTags = "Handmade $craftType, Traditional Indian Craft, Artisan Decor, Authentic Handloom",
                isGeneratedOffline = false
            )
        }
    }

    suspend fun calculateDynamicPricing(
        matCost: Double,
        labCost: Double,
        othCost: Double,
        productName: String = "Artisan Product",
        category: String = "Handicraft",
        craft: String = "Handloom",
        material: String = "Natural",
        technique: String = "Handmade"
    ): PricingResult = withContext(Dispatchers.IO) {
        val rec = DynamicPricingEngine.calculatePriceRecommendation(
            productName = productName,
            category = category,
            craft = craft,
            material = material,
            technique = technique,
            matCost = matCost,
            labCost = labCost,
            othCost = othCost
        )
        PricingResult(
            costFloor = rec.costFloor,
            recommendedMin = rec.suggestedMin,
            recommendedPrice = rec.recommendedPrice,
            recommendedMax = rec.suggestedMax,
            reasoning = rec.explanationText
        )
    }
}
