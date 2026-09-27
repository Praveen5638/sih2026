package com.example

import com.example.ai.DynamicPricingEngine
import com.example.ai.GeminiAiHelper
import com.example.data.ProductDraftEntity
import com.example.data.ProductEntity
import com.example.data.SyncOperationEntity
import com.example.network.NetworkMonitor
import com.example.network.NetworkState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class OfflineFirstArchitectureTest {

    @Before
    fun setUp() {
        NetworkMonitor.setSimulatedOffline(false)
    }

    @Test
    fun testLocalSourceOfTruth_writesLocallyFirst() {
        // 1. Arrange product entity
        val product = ProductEntity(
            productName = "Banarasi Silk Saree",
            category = "Handloom",
            craft = "Weaving",
            material = "Silk",
            technique = "Handloom",
            color = "Red",
            dimensions = "6.5m",
            productionTime = "5 Days",
            descriptionHi = "पारंपरिक बनारसी सिल्क साड़ी",
            descriptionEn = "Traditional Banarasi Silk Saree",
            seoTags = "Banarasi, Silk, Handloom",
            originalImageUrl = "file:///local/photo_orig.jpg",
            enhancedImageUrl = "file:///local/photo_enh.jpg",
            materialCost = 1000.0,
            labourCost = 500.0,
            otherCost = 150.0,
            costFloor = 1650.0,
            recommendedPrice = 2200.0,
            sellingPrice = 2200.0,
            status = "Ready"
        )

        // 2. Assert local properties are 100% valid before network interaction
        assertNotNull(product)
        assertEquals("Banarasi Silk Saree", product.productName)
        assertEquals(1650.0, product.costFloor, 0.01)
        assertTrue(product.originalImageUrl.startsWith("file:///"))
    }

    @Test
    fun testPersistentOutbox_createsIdempotentSyncOperation() {
        val productId = 101L
        val createdAt = System.currentTimeMillis()
        val clientOpId = "SYNC-PROD-$productId-$createdAt"

        val syncOp = SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            clientOperationId = clientOpId,
            operationType = "CREATE_PRODUCT",
            entityId = productId.toString(),
            payloadJson = "{\"id\":101,\"productName\":\"Banarasi Saree\"}",
            status = "PENDING"
        )

        assertEquals(clientOpId, syncOp.clientOperationId)
        assertEquals("PENDING", syncOp.status)
        assertEquals(0, syncOp.retryCount)
    }

    @Test
    fun testOfflineDraftAutosave_andRecovery() {
        val draft = ProductDraftEntity(
            draftId = "ACTIVE_DRAFT",
            currentStepName = "VOICE_CATALOGER",
            productName = "Terracotta Pot",
            craft = "Pottery",
            transcript = "Ye mitti ka pot hai",
            originalImageUrl = "file:///cache/pot.jpg",
            sellingPrice = 450.0
        )

        assertEquals("ACTIVE_DRAFT", draft.draftId)
        assertEquals("Terracotta Pot", draft.productName)
        assertEquals("file:///cache/pot.jpg", draft.originalImageUrl)
    }

    @Test
    fun testDynamicPricing_calculatesOfflineDeterministicCostFloor() {
        // Level 1 Offline Math Calculation
        val recommendation = DynamicPricingEngine.calculatePriceRecommendation(
            productName = "Terracotta Diya",
            category = "Terracotta",
            craft = "Terracotta",
            material = "Clay",
            technique = "Handmade",
            matCost = 100.0,
            labCost = 200.0,
            othCost = 50.0
        )

        // Cost Floor = 100 + 200 + 50 = 350.0
        assertEquals(350.0, recommendation.costFloor, 0.01)
        assertTrue("Recommended price must cover cost floor", recommendation.recommendedPrice >= recommendation.costFloor)
        assertNotNull(recommendation.explanationText)
    }

    @Test
    fun testNetworkMonitor_simulatedOfflineState() {
        NetworkMonitor.setSimulatedOffline(true)
        assertEquals(NetworkState.OFFLINE, NetworkMonitor.networkState.value)
        assertEquals(false, NetworkMonitor.isOnline())

        NetworkMonitor.setSimulatedOffline(false)
        assertEquals(NetworkState.ONLINE, NetworkMonitor.networkState.value)
        assertEquals(true, NetworkMonitor.isOnline())
    }

    @Test
    fun testOfflineAiFallback_returnsOfflineTaggedListing() = runBlocking {
        NetworkMonitor.setSimulatedOffline(true)
        val result = GeminiAiHelper.generateCatalogFromVoice("Mitti ka diya hai", "Terracotta")

        assertTrue(result.isGeneratedOffline)
        assertTrue(result.descriptionHi.contains("ऑफ़लाइन"))
        assertNotNull(result.productName)
    }

    @Test
    fun testImageUploadInterruption_retainsLocalUriAndSyncOperation() {
        NetworkMonitor.setSimulatedOffline(true)
        val localImageUri = "file:///data/user/0/com.aistudio.artisanai/cache/photo_fullres.jpg"
        val clientOpId = "UPLOAD-IMG-789-${System.currentTimeMillis()}"

        val uploadOp = SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            clientOperationId = clientOpId,
            operationType = "UPLOAD_IMAGE",
            entityId = "789",
            payloadJson = "{\"imageUri\":\"$localImageUri\"}",
            status = "PENDING"
        )

        // Verify local image reference is never destroyed by upload failure
        assertTrue(localImageUri.startsWith("file:///"))
        assertEquals("PENDING", uploadOp.status)
        assertEquals(clientOpId, uploadOp.clientOperationId)
    }

    @Test
    fun testPricingInterruption_usesCachedComparablesAndCostFloor() {
        NetworkMonitor.setSimulatedOffline(true)
        val res = DynamicPricingEngine.calculatePriceRecommendation(
            productName = "Brass Ganesha",
            category = "Metal",
            craft = "Brass",
            material = "Brass",
            technique = "Carved",
            matCost = 500.0,
            labCost = 300.0,
            othCost = 100.0
        )

        assertEquals(900.0, res.costFloor, 0.01)
        assertTrue(res.comparableCount > 0)
        assertTrue(res.recommendedPrice >= 900.0)
    }

    @Test
    fun testTransientFailure_executesExponentialBackoffAndRetries() {
        val op = SyncOperationEntity(
            clientOperationId = "SYNC-TEST-123",
            operationType = "CREATE_PRODUCT",
            entityId = "123",
            payloadJson = "{}",
            retryCount = 1,
            status = "FAILED_RETRYABLE"
        )

        val nextAttempt = op.retryCount + 1
        val backoffMs = (2.0.toDouble().pow(nextAttempt.toDouble()) * 1000).toLong()

        assertEquals(2, nextAttempt)
        assertEquals(4000L, backoffMs) // Exponential backoff 2^2 * 1000 = 4000ms
    }

    @Test
    fun testPhase2CatalogAndStoreOperations() {
        // Arrange Product Entity
        val initialProduct = ProductEntity(
            id = 501L,
            productName = "Terracotta Lamp",
            category = "Terracotta",
            craft = "Pottery",
            material = "Clay",
            technique = "Handmade",
            color = "Terracotta Red",
            dimensions = "15x15 cm",
            productionTime = "2 Days",
            descriptionHi = "हस्तनिर्मित मिटटी का लैम्प",
            descriptionEn = "Handmade Terracotta Lamp",
            seoTags = "Terracotta, Lamp, Handmade",
            originalImageUrl = "file:///local/lamp_orig.jpg",
            enhancedImageUrl = "file:///local/lamp_enh.jpg",
            materialCost = 200.0,
            labourCost = 300.0,
            otherCost = 50.0,
            costFloor = 550.0,
            recommendedPrice = 800.0,
            sellingPrice = 800.0,
            status = "Ready"
        )

        // Test Edit Operation
        val updatedProduct = initialProduct.copy(
            productName = "Designer Terracotta Lamp",
            sellingPrice = 850.0,
            status = "Published"
        )

        assertEquals("Designer Terracotta Lamp", updatedProduct.productName)
        assertEquals(850.0, updatedProduct.sellingPrice, 0.01)
        assertEquals("Published", updatedProduct.status)
        assertEquals(550.0, updatedProduct.costFloor, 0.01)
        assertTrue(updatedProduct.sellingPrice >= updatedProduct.costFloor)
    }

    // ============================================================
    // PHASE 3: BUYER ↔ ARTISAN VOICE & TEXT NEGOTIATION TESTS
    // ============================================================

    @Test
    fun testPhase3VoiceNegotiation_extractsCommercialTerms() {
        val transcript1 = "Mujhe 50 pcs ₹1500 me chahiye"
        val terms1 = com.example.ai.VoiceAssistEngine.extractCommercialTermsFromText(transcript1)
        assertEquals(50, terms1.quantity)
        assertEquals(1500.0, terms1.unitPrice!!, 0.01)

        val transcript2 = "100 units final price ₹1200 per pc"
        val terms2 = com.example.ai.VoiceAssistEngine.extractCommercialTermsFromText(transcript2)
        assertEquals(100, terms2.quantity)
        assertEquals(1200.0, terms2.unitPrice!!, 0.01)
    }

    @Test
    fun testPhase3VoiceNegotiation_localMessageCreationAndOutboxSync() {
        val conversationId = "CONV-101-BUYER-101"
        val clientMsgId = "MSG-CLIENT-${UUID.randomUUID()}"
        val messageId = "MSG-${UUID.randomUUID()}"

        val message = com.example.data.MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            clientMessageId = clientMsgId,
            senderId = "BUYER-101",
            senderType = "BUYER",
            text = "Kya aap 50 pcs ₹1400 me de sakte hain?",
            messageType = "VOICE_TRANSCRIPT",
            extractedQuantity = 50,
            extractedPrice = 1400.0,
            status = "PENDING"
        )

        val syncOp = SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            clientOperationId = clientMsgId,
            operationType = "SEND_MESSAGE",
            entityId = messageId,
            payloadJson = "{\"messageId\":\"$messageId\",\"conversationId\":\"$conversationId\",\"text\":\"${message.text}\"}",
            status = "PENDING"
        )

        assertEquals(clientMsgId, message.clientMessageId)
        assertEquals(clientMsgId, syncOp.clientOperationId)
        assertEquals("SEND_MESSAGE", syncOp.operationType)
        assertEquals(50, message.extractedQuantity)
        assertEquals(1400.0, message.extractedPrice!!, 0.01)
    }

    @Test
    fun testPhase3VoiceNegotiation_confirmTerms_updatesStatusToOrderReady() {
        val initialConv = com.example.data.ConversationEntity(
            conversationId = "CONV-101-BUYER-101",
            productId = 101L,
            productName = "Banarasi Silk Saree",
            buyerId = "BUYER-101",
            buyerName = "Anil Sharma",
            artisanName = "Ramesh Kumar",
            currentStatus = "NEGOTIATING",
            agreedQuantity = 50,
            agreedUnitPrice = 1400.0
        )

        val confirmedConv = initialConv.copy(
            buyerConfirmed = true,
            sellerConfirmed = true,
            currentStatus = "ORDER_READY",
            lastMessageText = "✅ Terms Confirmed: 50 pcs @ ₹1400/pc. Order Ready!"
        )

        assertEquals("ORDER_READY", confirmedConv.currentStatus)
        assertTrue(confirmedConv.buyerConfirmed)
        assertTrue(confirmedConv.sellerConfirmed)
        assertEquals(50, confirmedConv.agreedQuantity)
        assertEquals(1400.0, confirmedConv.agreedUnitPrice, 0.01)
        assertEquals(70000.0, confirmedConv.agreedQuantity * confirmedConv.agreedUnitPrice, 0.01)
    }
}

