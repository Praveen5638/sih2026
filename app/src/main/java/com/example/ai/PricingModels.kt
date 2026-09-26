package com.example.ai

enum class PricingConflictStatus {
    NORMAL,
    MILD_WARNING,
    COST_MARKET_CONFLICT,
    LOW_PRICE_WARNING,
    LIMITED_MARKET_DATA
}

enum class MarketConfidence {
    HIGH,
    MEDIUM,
    LOW
}

data class MarketComparable(
    val title: String,
    val category: String,
    val craft: String,
    val material: String,
    val technique: String,
    val price: Double,
    val marketType: String = "RETAIL", // "RETAIL" or "B2B_WHOLESALE"
    val source: String = "Curated Indian Handicraft Benchmark",
    val isHandmade: Boolean = true,
    val similarityScore: Float = 1.0f
)

data class CostBreakdown(
    val materialCost: Double,
    val labourCost: Double,
    val packagingTransportCost: Double,
    val otherDirectCost: Double = 0.0,
    val directCostFloor: Double = materialCost + labourCost + packagingTransportCost + otherDirectCost,
    val targetMarginPercent: Double = 30.0,
    val targetPrice: Double = directCostFloor / (1.0 - (targetMarginPercent / 100.0).coerceAtMost(0.90))
)

data class PricingRecommendationResult(
    val costFloor: Double,
    val targetPrice: Double,
    val marketP25: Double,
    val marketMedian: Double,
    val marketP75: Double,
    val suggestedMin: Double,
    val recommendedPrice: Double,
    val suggestedMax: Double,
    val conflictStatus: PricingConflictStatus,
    val marketConfidence: MarketConfidence,
    val comparableCount: Int,
    val explanationText: String,
    val warningText: String? = null,
    val pricingEngineVersion: String = "v1-hardened",
    val timestamp: Long = System.currentTimeMillis()
)
