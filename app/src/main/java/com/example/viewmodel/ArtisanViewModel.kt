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
    var pricingRecommendationResult by mutableStateOf<com.example.ai.PricingRecommendationResult?>(null)
    var targetProfitMarginInput by mutableStateOf(30.0)
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
        targetProfitMarginInput = 30.0
        pricingResult = null
        pricingRecommendationResult = null
    }

    var studioStageLogs by mutableStateOf<List<com.example.ai.ProcessingStageLog>>(emptyList())
    var studioMetrics by mutableStateOf<com.example.ai.StudioPipelineMetrics?>(null)
    var imageContractResult by mutableStateOf<com.example.ai.ImageProcessingContractResult?>(null)
    var benchmarkResults by mutableStateOf<List<com.example.ai.BenchmarkItemResult>>(emptyList())
    var userSelectedPhotoChoice by mutableStateOf("ENHANCED") // "ENHANCED" or "ORIGINAL"

    fun enhanceImage(imageUri: String) {
        originalImageUri = imageUri
        enhancedImageUri = imageUri
    }

    fun processCameraUriInput(context: android.content.Context, inputUri: android.net.Uri, onComplete: () -> Unit) {
        viewModelScope.launch {
            isAiProcessing = true
            val result = com.example.ai.ProductImageProcessor.processUriInput(context, inputUri)
            imageContractResult = result
            originalImageUri = result.originalUri
            enhancedImageUri = result.enhancedUri ?: result.originalUri
            studioStageLogs = result.stageLogs
            userSelectedPhotoChoice = "ENHANCED"
            isAiProcessing = false
            onComplete()
        }
    }

    fun processCameraBitmapInput(context: android.content.Context, inputBitmap: android.graphics.Bitmap, onComplete: () -> Unit) {
        viewModelScope.launch {
            isAiProcessing = true
            val result = com.example.ai.ProductImageProcessor.processBitmapInput(context, inputBitmap)
            imageContractResult = result
            originalImageUri = result.originalUri
            enhancedImageUri = result.enhancedUri ?: result.originalUri
            studioStageLogs = result.stageLogs
            userSelectedPhotoChoice = "ENHANCED"
            isAiProcessing = false
            onComplete()
        }
    }

    fun userSelectEnhancedPhoto() {
        userSelectedPhotoChoice = "ENHANCED"
        enhancedImageUri = imageContractResult?.enhancedUri ?: originalImageUri
    }

    fun userSelectOriginalPhoto() {
        userSelectedPhotoChoice = "ORIGINAL"
        enhancedImageUri = originalImageUri
    }

    fun runStudioBenchmark() {
        viewModelScope.launch {
            isAiProcessing = true
            benchmarkResults = com.example.ai.ProductImageProcessor.runCraftBenchmarkSuite()
            isAiProcessing = false
        }
    }

    var listingSession by mutableStateOf(com.example.ai.ListingSession())

    fun startVoiceListening(context: android.content.Context, onTranscript: (String) -> Unit = {}) {
        com.example.ai.VoiceAssistEngine.initializeTts(context)
        listingSession = listingSession.copy(
            isRecording = true,
            currentStage = com.example.ai.ConversationStage.LISTENING
        )
        com.example.ai.VoiceAssistEngine.startListening(
            context = context,
            onResult = { text ->
                listingSession = listingSession.copy(
                    isRecording = false,
                    transcript = text,
                    currentStage = com.example.ai.ConversationStage.TRANSCRIBING
                )
                onTranscript(text)
                processConversationalSpeech(context, text) {}
            },
            onError = { error ->
                listingSession = listingSession.copy(
                    isRecording = false,
                    currentStage = com.example.ai.ConversationStage.ERROR,
                    currentQuestion = error
                )
            }
        )
    }

    fun stopVoiceListening() {
        com.example.ai.VoiceAssistEngine.stopListening()
        listingSession = listingSession.copy(isRecording = false)
    }

    fun processConversationalSpeech(context: android.content.Context, transcript: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isAiProcessing = true
            voiceTranscript = transcript
            listingSession = listingSession.copy(
                transcript = transcript,
                currentStage = com.example.ai.ConversationStage.EXTRACTING
            )

            // Slot filling
            val slots = com.example.ai.VoiceAssistEngine.extractSlotsFromSpeech(transcript, artisanCraft)
            if (!slots.productName.isNullOrBlank()) productName = slots.productName
            if (!slots.material.isNullOrBlank()) material = slots.material
            if (!slots.color.isNullOrBlank()) color = slots.color
            if (!slots.technique.isNullOrBlank()) technique = slots.technique
            if (!slots.productionTime.isNullOrBlank()) productionTime = slots.productionTime

            // Structured LLM generation
            val result = GeminiAiHelper.generateCatalogFromVoice(transcript, artisanCraft)
            productName = slots.productName ?: result.productName
            category = slots.category ?: result.category
            craft = slots.craft ?: result.craft
            material = slots.material ?: result.material
            technique = slots.technique ?: result.technique
            color = slots.color ?: result.color
            dimensions = result.dimensions.ifBlank { "Not specified" }
            productionTime = slots.productionTime ?: result.productionTime
            descriptionHi = result.descriptionHi
            descriptionEn = result.descriptionEn
            seoTags = result.seoTags

            listingSession = listingSession.copy(
                productName = productName,
                category = category,
                craft = craft,
                material = material,
                color = color,
                technique = technique,
                dimensions = dimensions,
                productionTime = productionTime,
                descriptionHindi = descriptionHi,
                descriptionEnglish = descriptionEn,
                seoTags = seoTags,
                currentStage = com.example.ai.ConversationStage.REVIEWING
            )

            isAiProcessing = false
            onComplete()
        }
    }

    fun processVoiceCommand(context: android.content.Context, commandText: String) {
        val classified = com.example.ai.VoiceAssistEngine.classifyVoiceCommand(commandText)
        listingSession = listingSession.copy(lastVoiceCommand = commandText)

        when (classified.intent) {
            "SAVE_PRODUCT" -> {
                saveCurrentProduct { currentScreen = AppScreen.HOME }
            }
            "NAVIGATE_PRICING" -> {
                currentScreen = AppScreen.PRICING_ASSISTANT
            }
            "REPEAT_TTS" -> {
                speakListingSummary(context)
            }
            "EDIT_FIELD" -> {
                when (classified.targetField) {
                    "productName" -> productName = classified.newValue ?: productName
                    "material" -> material = classified.newValue ?: material
                    "color" -> color = classified.newValue ?: color
                    "technique" -> technique = classified.newValue ?: technique
                    "productionTime" -> productionTime = classified.newValue ?: productionTime
                }
                speakListingSummary(context)
            }
            "CONFIRM" -> {
                currentScreen = AppScreen.PRICING_ASSISTANT
            }
        }
    }

    fun speakListingSummary(context: android.content.Context) {
        com.example.ai.VoiceAssistEngine.initializeTts(context) {
            val summaryText = "Product $productName ready hai. Material $material, Technique $technique hai. Kya details sahi hain?"
            listingSession = listingSession.copy(isSpeakingTTS = true)
            com.example.ai.VoiceAssistEngine.speakReadBack(summaryText) {
                listingSession = listingSession.copy(isSpeakingTTS = false)
            }
        }
    }

    fun processVoiceTranscript(transcript: String, onComplete: () -> Unit) {
        processConversationalSpeech(getApplication(), transcript, onComplete)
    }

    fun calculatePricing(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isAiProcessing = true
            val mat = materialCostInput.toDoubleOrNull() ?: 800.0
            val lab = labourCostInput.toDoubleOrNull() ?: 400.0
            val oth = otherCostInput.toDoubleOrNull() ?: 120.0
            val targetMargin = targetProfitMarginInput
            val currentSellingPrice = sellingPriceInput.toDoubleOrNull()

            val rec = com.example.ai.DynamicPricingEngine.calculatePriceRecommendation(
                productName = productName.ifBlank { "Artisan Craft" },
                category = category.ifBlank { "Handicraft" },
                craft = craft.ifBlank { artisanCraft },
                material = material.ifBlank { "Handloom / Handicraft" },
                technique = technique.ifBlank { "Handmade" },
                matCost = mat,
                labCost = lab,
                othCost = oth,
                targetMargin = targetMargin,
                userSelectedPrice = currentSellingPrice
            )

            pricingRecommendationResult = rec
            pricingResult = PricingResult(
                costFloor = rec.costFloor,
                recommendedMin = rec.suggestedMin,
                recommendedPrice = rec.recommendedPrice,
                recommendedMax = rec.suggestedMax,
                reasoning = rec.explanationText
            )

            if (sellingPriceInput.isBlank() || sellingPriceInput == "1750") {
                sellingPriceInput = rec.recommendedPrice.toInt().toString()
            }
            isAiProcessing = false
            onComplete()
        }
    }

    fun speakPricingSummary(context: android.content.Context) {
        com.example.ai.VoiceAssistEngine.initializeTts(context) {
            val rec = pricingRecommendationResult
            val text = if (rec != null) {
                "Suniye: Aapka production cost floor ₹${rec.costFloor.toInt()} hai, jisme labour cost ₹${labourCostInput} fully protected hai. Market median ₹${rec.marketMedian.toInt()} hai. Suggested fair price ₹${rec.recommendedPrice.toInt()} hai."
            } else {
                "Aapka target price ₹$sellingPriceInput set kiya gaya hai."
            }
            com.example.ai.VoiceAssistEngine.speakReadBack(text)
        }
    }

    fun saveCurrentProduct(status: String = "Ready", onSaved: () -> Unit) {
        viewModelScope.launch {
            val mat = materialCostInput.toDoubleOrNull() ?: 800.0
            val lab = labourCostInput.toDoubleOrNull() ?: 400.0
            val oth = otherCostInput.toDoubleOrNull() ?: 120.0
            val rec = pricingRecommendationResult
            val floor = rec?.costFloor ?: (mat + lab + oth)
            val recPrice = rec?.recommendedPrice ?: (floor * 1.4)
            val sell = sellingPriceInput.toDoubleOrNull() ?: recPrice

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
                recommendedPrice = recPrice,
                sellingPrice = sell,
                marketP25 = rec?.marketP25 ?: (floor * 1.25),
                marketMedian = rec?.marketMedian ?: (floor * 1.35),
                marketP75 = rec?.marketP75 ?: (floor * 1.50),
                marketConfidence = rec?.marketConfidence?.name ?: "MEDIUM",
                pricingEngineVersion = rec?.pricingEngineVersion ?: "v1-hardened",
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
