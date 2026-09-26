package com.example.ai

import kotlin.math.max
import kotlin.math.min

object DynamicPricingEngine {

    // Curated Benchmark Dataset of Indian Artisan Market Comparables
    private val marketBenchmarkDatabase = listOf(
        // Banarasi Saree / Handloom Silk
        MarketComparable("Banarasi Katan Silk Saree", "Handloom", "Banarasi", "Silk", "Zari Handweave", 1850.0, "RETAIL"),
        MarketComparable("Traditional Banarasi Silk Saree", "Handloom", "Banarasi", "Silk", "Zari Work", 1750.0, "RETAIL"),
        MarketComparable("Pure Silk Banarasi Brocade Saree", "Handloom", "Banarasi", "Silk", "Handloom", 1950.0, "RETAIL"),
        MarketComparable("Banarasi Tissue Silk Saree", "Handloom", "Banarasi", "Silk", "Zari Handweave", 1650.0, "RETAIL"),
        MarketComparable("Handwoven Silk Saree", "Handloom", "Weaving", "Silk", "Handloom", 1600.0, "RETAIL"),

        // Terracotta / Pottery / Earthen Crafts
        MarketComparable("Handcrafted Terracotta Diya Set", "Terracotta", "Terracotta", "Terracotta / Clay", "Handmade", 450.0, "RETAIL"),
        MarketComparable("Traditional Clay Earthen Diya", "Terracotta", "Pottery", "Terracotta / Clay", "Pottery", 380.0, "RETAIL"),
        MarketComparable("Designer Terracotta Vase", "Terracotta", "Terracotta", "Terracotta / Clay", "Hand Painted", 650.0, "RETAIL"),

        // Brass / Metal Crafts
        MarketComparable("Brass Ganesha Idol", "Metal", "Brass", "Brass", "Hand Carved", 1450.0, "RETAIL"),
        MarketComparable("Handcrafted Brass Pooja Bell", "Metal", "Brass", "Brass", "Handmade", 850.0, "RETAIL"),
        MarketComparable("Brass Decorative Statue", "Metal", "Brass", "Brass", "Hand Carved", 1600.0, "RETAIL"),

        // Bamboo / Wood Crafts
        MarketComparable("Bamboo Handwoven Basket", "Handicraft", "Bamboo", "Natural Bamboo", "Handweave", 550.0, "RETAIL"),
        MarketComparable("Carved Wooden Elephant Pair", "Handicraft", "Wood carving", "Carved Wood", "Hand Carved", 1250.0, "RETAIL")
    )

    fun calculatePriceRecommendation(
        productName: String,
        category: String,
        craft: String,
        material: String,
        technique: String,
        matCost: Double,
        labCost: Double,
        othCost: Double,
        targetMargin: Double = 30.0,
        userSelectedPrice: Double? = null
    ): PricingRecommendationResult {
        // 1. DETERMINISTIC COST MODEL (Stream A: Seller Economics)
        val costBreakdown = CostBreakdown(
            materialCost = matCost,
            labourCost = labCost,
            packagingTransportCost = othCost,
            targetMarginPercent = targetMargin
        )
        val costFloor = costBreakdown.directCostFloor
        val targetPrice = costBreakdown.targetPrice

        // 2. SIMILARITY SCORING & MARKET COMPARABLES (Stream B: Market Data)
        val scoredComparables = marketBenchmarkDatabase.map { comp ->
            var score = 0.0f
            if (comp.craft.equals(craft, ignoreCase = true)) score += 0.35f
            if (comp.material.contains(material, ignoreCase = true) || material.contains(comp.material, ignoreCase = true)) score += 0.25f
            if (comp.technique.contains(technique, ignoreCase = true) || technique.contains(comp.technique, ignoreCase = true)) score += 0.20f
            if (comp.category.equals(category, ignoreCase = true)) score += 0.20f
            
            // Fallback match boost
            if (score == 0f && productName.contains(comp.craft, ignoreCase = true)) score += 0.25f

            comp.copy(similarityScore = score)
        }.filter { it.similarityScore >= 0.20f }
            .sortedByDescending { it.similarityScore }

        val comparableCount = scoredComparables.size
        val prices = scoredComparables.map { it.price }.sorted()

        // 3. ROBUST STATISTICS (P25, Median P50, P75)
        val marketP25: Double
        val marketMedian: Double
        val marketP75: Double

        if (prices.isNotEmpty()) {
            marketP25 = calculatePercentile(prices, 25.0)
            marketMedian = calculatePercentile(prices, 50.0)
            marketP75 = calculatePercentile(prices, 75.0)
        } else {
            marketP25 = costFloor * 1.20
            marketMedian = costFloor * 1.35
            marketP75 = costFloor * 1.50
        }

        // 4. DATA CONFIDENCE ASSESSMENT
        val confidence = when {
            comparableCount >= 4 -> MarketConfidence.HIGH
            comparableCount in 2..3 -> MarketConfidence.MEDIUM
            else -> MarketConfidence.LOW
        }

        // 5. CONFLICT DETECTION ENGINE
        val conflictStatus: PricingConflictStatus
        var warningText: String? = null

        if (costFloor > marketP75 && comparableCount >= 2) {
            conflictStatus = PricingConflictStatus.COST_MARKET_CONFLICT
            warningText = "Aapka sustainable cost (₹${costFloor.toInt()}) similar products ke market range (₹${marketP25.toInt()}–₹${marketP75.toInt()}) se upar hai."
        } else if (userSelectedPrice != null && userSelectedPrice < costFloor) {
            conflictStatus = PricingConflictStatus.LOW_PRICE_WARNING
            val loss = (costFloor - userSelectedPrice).toInt()
            warningText = "Selected price ₹${userSelectedPrice.toInt()} aapke production cost ₹${costFloor.toInt()} se kam hai. Per unit loss: ₹$loss."
        } else if (targetPrice > marketP75) {
            conflictStatus = PricingConflictStatus.MILD_WARNING
            warningText = "Aapka target price (₹${targetPrice.toInt()}) market P75 (₹${marketP75.toInt()}) se thoda upar hai."
        } else if (comparableCount < 2) {
            conflictStatus = PricingConflictStatus.LIMITED_MARKET_DATA
            warningText = "Limited market data available for exact craft match."
        } else {
            conflictStatus = PricingConflictStatus.NORMAL
        }

        // 6. RECOMMENDED PRICE CALCULATION
        val suggestedMin = max(costFloor, marketP25)
        val suggestedMax = max(suggestedMin * 1.15, marketP75)
        
        val recommendedPrice = if (conflictStatus == PricingConflictStatus.COST_MARKET_CONFLICT) {
            costFloor // Never underprice below cost floor even in conflict!
        } else {
            // Balance cost floor coverage with market median
            max(targetPrice, marketMedian).coerceIn(suggestedMin, suggestedMax)
        }

        // 7. EXPLAINABLE PRICE GENERATION
        val explanationText = StringBuilder().apply {
            append("• Estimated Production Cost: ₹${costFloor.toInt()} (Material: ₹${matCost.toInt()} + Labour: ₹${labCost.toInt()} + Transport/Packaging: ₹${othCost.toInt()})\n")
            if (prices.isNotEmpty()) {
                append("• Observed Comparable Market Range: ₹${marketP25.toInt()} – ₹${marketP75.toInt()} (Based on $comparableCount similar $craft items)\n")
            } else {
                append("• Market Range: Estimated based on cost floor + 35% artisan fair margin\n")
            }
            append("• Recommendation Basis: Covers 100% of your production cost & labour effort while remaining competitive in the artisan marketplace.")
        }.toString()

        return PricingRecommendationResult(
            costFloor = costFloor,
            targetPrice = targetPrice,
            marketP25 = marketP25,
            marketMedian = marketMedian,
            marketP75 = marketP75,
            suggestedMin = suggestedMin,
            recommendedPrice = recommendedPrice,
            suggestedMax = suggestedMax,
            conflictStatus = conflictStatus,
            marketConfidence = confidence,
            comparableCount = comparableCount,
            explanationText = explanationText,
            warningText = warningText
        )
    }

    private fun calculatePercentile(sortedList: List<Double>, percentile: Double): Double {
        if (sortedList.isEmpty()) return 0.0
        if (sortedList.size == 1) return sortedList[0]
        val index = (percentile / 100.0) * (sortedList.size - 1)
        val lower = index.toInt()
        val upper = min(lower + 1, sortedList.size - 1)
        val weight = index - lower
        return sortedList[lower] * (1.0 - weight) + sortedList[upper] * weight
    }
}
