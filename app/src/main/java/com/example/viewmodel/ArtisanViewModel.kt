package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiHelper
import com.example.ai.PricingResult
import com.example.ai.ProductListingResult
import com.example.data.AppDatabase
import com.example.data.ArtisanRepository
import com.example.data.ProductEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppScreen {
    MARKETPLACE_HOME,      // Default Public Buyer Catalog Home Page
    BUYER_LOGIN,           
    BUYER_ORDERS,          // Buyer tracking orders
    BUYER_CHECKOUT,        
    SELLER_LOGIN,          
    SELLER_PROFILE,        
    SELLER_HOME,           
    // Legacy / backward compatible enums used across screens
    SPLASH,
    LANGUAGE,
    AUTH,
    PROFILE,
    HOME,
    BUYER_MARKETPLACE,
    CREATE_PRODUCT,
    IMAGE_STUDIO,
    VOICE_CATALOGER,
    AI_REVIEW,
    PRICING_ASSISTANT,
    FINAL_PREVIEW,
    CATALOG,
    PRODUCT_DETAIL,
    PUBLIC_PRODUCT_DETAIL
}

data class BuyerOrder(
    val orderId: String,
    val productName: String,
    val amount: Double,
    val quantity: Int,
    val address: String,
    val status: String = "Dispatched 🚚"
)

class ArtisanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ArtisanRepository

    init {
        val productDao = AppDatabase.getDatabase(application).productDao()
        repository = ArtisanRepository(productDao)
    }

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var currentScreen by mutableStateOf(AppScreen.MARKETPLACE_HOME)
    var selectedLanguage by mutableStateOf("Hindi")
    
    // Seller State
    var sellerMobile by mutableStateOf("")
    var mobileNumber by mutableStateOf("")
    var artisanName by mutableStateOf("Ramesh Kumar")
    var artisanCraft by mutableStateOf("Handloom Weaver")
    var artisanLocation by mutableStateOf("Varanasi, Uttar Pradesh")

    // Buyer State
    var buyerMobile by mutableStateOf("")
    var buyerName by mutableStateOf("")
    var isBuyerLoggedIn by mutableStateOf(false)
    val buyerOrders = mutableStateListOf<BuyerOrder>()

    fun placeBuyerOrder(productName: String, amount: Double, qty: Int, address: String) {
        val newOrder = BuyerOrder(
            orderId = "ONDC-${(1000..9999).random()}",
            productName = productName,
            amount = amount,
            quantity = qty,
            address = address,
            status = "Confirmed & Dispatched 🚚"
        )
        buyerOrders.add(0, newOrder)
    }

    // Product Creation Draft
    var originalImageUri by mutableStateOf("")
    var enhancedImageUri by mutableStateOf("")
    var voiceTranscript by mutableStateOf("")
    var productName by mutableStateOf("")
    var category by mutableStateOf("")
    var craft by mutableStateOf("")
    var material by mutableStateOf("")
    var technique by mutableStateOf("")
    var color by mutableStateOf("")
    var dimensions by mutableStateOf("")
    var productionTime by mutableStateOf("")
    var descriptionHi by mutableStateOf("")
    var descriptionEn by mutableStateOf("")
    var seoTags by mutableStateOf("")

    var materialCostInput by mutableStateOf("800")
    var labourCostInput by mutableStateOf("400")
    var otherCostInput by mutableStateOf("120")
    var sellingPriceInput by mutableStateOf("1750")

    var pricingResult by mutableStateOf<PricingResult?>(null)
    var selectedProductIdForDetail by mutableStateOf<Long?>(null)
    var isAiProcessing by mutableStateOf(false)

    fun resetProductDraft() {
        originalImageUri = ""
        enhancedImageUri = ""
        voiceTranscript = ""
        productName = ""
        category = ""
        craft = artisanCraft
        material = ""
        technique = ""
        color = ""
        dimensions = ""
        productionTime = ""
        descriptionHi = ""
        descriptionEn = ""
        seoTags = ""
        materialCostInput = "800"
        labourCostInput = "400"
        otherCostInput = "120"
        sellingPriceInput = "1750"
        pricingResult = null
    }

    var studioStageLogs by mutableStateOf<List<com.example.ai.ProcessingStageLog>>(emptyList())
    var maskQualityScore by mutableStateOf(0f)
    var fidelityScore by mutableStateOf(0f)

    fun enhanceImage(imageUri: String) {
        originalImageUri = imageUri
        enhancedImageUri = imageUri
    }

    fun processCameraImage(context: android.content.Context, inputBitmap: android.graphics.Bitmap, onComplete: () -> Unit) {
        viewModelScope.launch {
            isAiProcessing = true
            val result = com.example.ai.ProductImageProcessor.runStudioPipeline(context, inputBitmap)
            enhancedImageUri = result.outputUri.toString()
            originalImageUri = result.outputUri.toString()
            studioStageLogs = result.stageLogs
            maskQualityScore = result.maskQualityScore
            fidelityScore = result.fidelityScore
            isAiProcessing = false
            onComplete()
        }
    }

    fun processVoiceTranscript(transcript: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            isAiProcessing = true
            voiceTranscript = transcript
            val result = GeminiAiHelper.generateCatalogFromVoice(transcript, artisanCraft)
            productName = result.productName
            category = result.category
            craft = result.craft
            material = result.material
            technique = result.technique
            color = result.color
            dimensions = result.dimensions
            productionTime = result.productionTime
            descriptionHi = result.descriptionHi
            descriptionEn = result.descriptionEn
            seoTags = result.seoTags
            isAiProcessing = false
            onComplete()
        }
    }

    fun calculatePricing(onComplete: () -> Unit) {
        viewModelScope.launch {
            isAiProcessing = true
            val mat = materialCostInput.toDoubleOrNull() ?: 800.0
            val lab = labourCostInput.toDoubleOrNull() ?: 400.0
            val oth = otherCostInput.toDoubleOrNull() ?: 120.0
            val res = GeminiAiHelper.calculateDynamicPricing(mat, lab, oth)
            pricingResult = res
            if (sellingPriceInput.isBlank() || sellingPriceInput.toDoubleOrNull() == 1750.0) {
                sellingPriceInput = res.recommendedPrice.toInt().toString()
            }
            isAiProcessing = false
            onComplete()
        }
    }

    fun saveCurrentProduct(status: String = "Ready", onSaved: () -> Unit) {
        viewModelScope.launch {
            val mat = materialCostInput.toDoubleOrNull() ?: 800.0
            val lab = labourCostInput.toDoubleOrNull() ?: 400.0
            val oth = otherCostInput.toDoubleOrNull() ?: 120.0
            val floor = pricingResult?.costFloor ?: (mat + lab + oth)
            val rec = pricingResult?.recommendedPrice ?: (floor * 1.4)
            val sell = sellingPriceInput.toDoubleOrNull() ?: rec

            val entity = ProductEntity(
                productName = productName.ifBlank { "Handcrafted $artisanCraft" },
                category = category.ifBlank { "Handicraft" },
                craft = craft.ifBlank { artisanCraft },
                material = material.ifBlank { "Traditional Material" },
                technique = technique.ifBlank { "Handmade" },
                color = color.ifBlank { "Natural" },
                dimensions = dimensions.ifBlank { "Standard" },
                productionTime = productionTime.ifBlank { "2 Days" },
                descriptionHi = descriptionHi,
                descriptionEn = descriptionEn,
                seoTags = seoTags,
                originalImageUrl = originalImageUri,
                enhancedImageUrl = enhancedImageUri,
                materialCost = mat,
                labourCost = lab,
                otherCost = oth,
                costFloor = floor,
                recommendedPrice = rec,
                sellingPrice = sell,
                status = status
            )
            repository.insertProduct(entity)
            onSaved()
        }
    }

    fun deleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }
}
