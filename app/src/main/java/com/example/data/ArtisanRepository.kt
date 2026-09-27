package com.example.data

import android.content.Context
import com.example.network.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.UUID

class ArtisanRepository(
    private val productDao: ProductDao,
    private val draftDao: DraftDao? = null,
    private val syncDao: SyncDao? = null,
    private val conversationDao: ConversationDao? = null,
    private val messageDao: MessageDao? = null
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    val allConversations: Flow<List<ConversationEntity>> =
        conversationDao?.getAllConversationsFlow() ?: emptyFlow()

    fun getConversationById(conversationId: String): Flow<ConversationEntity?> =
        conversationDao?.getConversationFlow(conversationId) ?: emptyFlow()

    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> =
        messageDao?.getMessagesForConversationFlow(conversationId) ?: emptyFlow()

    suspend fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(id: Long) = productDao.deleteProduct(id)

    // WRITE LOCALLY FIRST + PERSISTENT OUTBOX SYNC QUEUE FOR PRODUCTS
    suspend fun saveProductLocallyFirst(context: Context, product: ProductEntity): Long {
        // 1. SAVE TO ROOM (AUTHORITATIVE LOCAL SOURCE OF TRUTH)
        val id = productDao.insertProduct(product)

        // 2. ENQUEUE PERSISTENT SYNC OPERATION FOR WORKMANAGER / SYNC MANAGER
        if (syncDao != null) {
            val clientOperationId = "SYNC-PROD-${id}-${product.createdAt}"
            val payload = """
                {"id":$id,"productName":"${product.productName}","sellingPrice":${product.sellingPrice},"status":"${product.status}"}
            """.trimIndent()

            val syncOperation = SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                clientOperationId = clientOperationId,
                operationType = "CREATE_PRODUCT",
                entityId = id.toString(),
                payloadJson = payload,
                status = "PENDING",
                retryCount = 0,
                createdAt = System.currentTimeMillis()
            )
            syncDao.insertOperation(syncOperation)
        }

        // 3. CLEAR DRAFT CHECKPOINT NOW THAT PRODUCT IS PERSISTED
        draftDao?.clearDraft()

        // 4. TRIGGER ASYNC BACKGROUND SYNC WHEN CONNECTIVITY IS AVAILABLE
        SyncManager.triggerSync(context)

        // 5. RETURN INSTANT SUCCESS TO UI FROM LOCAL PERSISTENCE
        return id
    }

    suspend fun saveDraftCheckpoint(draft: ProductDraftEntity) {
        draftDao?.saveDraft(draft)
    }

    suspend fun getRecoverableDraft(): ProductDraftEntity? {
        return draftDao?.getDraft()
    }

    suspend fun clearDraftCheckpoint() {
        draftDao?.clearDraft()
    }

    // ============================================================
    // PHASE 3: CONVERSATION & VOICE/TEXT NEGOTIATION MANAGEMENT
    // ============================================================

    suspend fun getOrCreateConversation(
        productId: Long,
        productName: String,
        buyerId: String,
        buyerName: String,
        artisanName: String,
        initialPrice: Double
    ): ConversationEntity {
        val conversationId = "CONV-${productId}-${buyerId}"
        val existing = conversationDao?.getConversation(conversationId)
        if (existing != null) {
            return existing
        }

        val newConv = ConversationEntity(
            conversationId = conversationId,
            productId = productId,
            productName = productName,
            buyerId = buyerId,
            buyerName = buyerName,
            artisanName = artisanName,
            currentStatus = "ENQUIRING",
            agreedQuantity = 1,
            agreedUnitPrice = initialPrice,
            buyerConfirmed = false,
            sellerConfirmed = false,
            lastMessageText = "Enquiry started for $productName",
            updatedAt = System.currentTimeMillis()
        )
        conversationDao?.insertConversation(newConv)
        return newConv
    }

    suspend fun sendMessageLocallyFirst(
        context: Context,
        conversationId: String,
        senderId: String,
        senderType: String,
        text: String,
        language: String = "hi",
        messageType: String = "TEXT",
        extractedQuantity: Int? = null,
        extractedPrice: Double? = null
    ): MessageEntity {
        val now = System.currentTimeMillis()
        val clientMsgId = "MSG-CLIENT-${UUID.randomUUID()}"
        val messageId = "MSG-${UUID.randomUUID()}"

        val message = MessageEntity(
            messageId = messageId,
            conversationId = conversationId,
            clientMessageId = clientMsgId,
            senderId = senderId,
            senderType = senderType,
            text = text,
            language = language,
            messageType = messageType,
            extractedQuantity = extractedQuantity,
            extractedPrice = extractedPrice,
            status = "PENDING",
            createdAt = now
        )

        // 1. PERSIST LOCALLY FIRST TO ROOM DB
        messageDao?.insertMessage(message)

        // 2. UPDATE CONVERSATION ROOM ENTITY
        conversationDao?.let { cDao ->
            val conv = cDao.getConversation(conversationId)
            if (conv != null) {
                val newStatus = if (extractedQuantity != null || extractedPrice != null) "NEGOTIATING" else conv.currentStatus
                val updatedConv = conv.copy(
                    lastMessageText = text,
                    agreedQuantity = extractedQuantity ?: conv.agreedQuantity,
                    agreedUnitPrice = extractedPrice ?: conv.agreedUnitPrice,
                    currentStatus = newStatus,
                    updatedAt = now
                )
                cDao.updateConversation(updatedConv)
            }
        }

        // 3. ENQUEUE OUTBOX PERSISTENT SYNC OPERATION FOR WORKMANAGER (WITH IDEMPOTENCY KEY)
        syncDao?.let { sDao ->
            val payload = """
                {"messageId":"$messageId","clientMessageId":"$clientMsgId","conversationId":"$conversationId","senderId":"$senderId","senderType":"$senderType","text":"${text.replace("\"", "\\\"")}","extractedQuantity":${extractedQuantity ?: "null"},"extractedPrice":${extractedPrice ?: "null"},"createdAt":$now}
            """.trimIndent()

            val syncOp = SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                clientOperationId = clientMsgId,
                operationType = "SEND_MESSAGE",
                entityId = messageId,
                payloadJson = payload,
                status = "PENDING",
                retryCount = 0,
                createdAt = now
            )
            sDao.insertOperation(syncOp)
        }

        // 4. TRIGGER BACKGROUND ASYNC SYNC
        SyncManager.triggerSync(context)

        return message
    }

    suspend fun confirmNegotiationTerms(
        context: Context,
        conversationId: String,
        confirmedQuantity: Int,
        confirmedUnitPrice: Double,
        confirmedByRole: String = "SELLER"
    ) {
        val now = System.currentTimeMillis()
        conversationDao?.let { cDao ->
            val conv = cDao.getConversation(conversationId)
            if (conv != null) {
                val updatedConv = conv.copy(
                    agreedQuantity = confirmedQuantity,
                    agreedUnitPrice = confirmedUnitPrice,
                    buyerConfirmed = conv.buyerConfirmed || confirmedByRole == "BUYER",
                    sellerConfirmed = conv.sellerConfirmed || confirmedByRole == "SELLER",
                    currentStatus = "ORDER_READY",
                    lastMessageText = "✅ Terms Confirmed: $confirmedQuantity pcs @ ₹${confirmedUnitPrice.toInt()}/pc. Order Ready!",
                    updatedAt = now
                )
                cDao.updateConversation(updatedConv)
            }
        }

        sendMessageLocallyFirst(
            context = context,
            conversationId = conversationId,
            senderId = confirmedByRole,
            senderType = confirmedByRole,
            text = "✅ Commercial terms agreed & locked: $confirmedQuantity pcs @ ₹${confirmedUnitPrice.toInt()}/pc. Status: ORDER_READY.",
            messageType = "CONFIRMATION",
            extractedQuantity = confirmedQuantity,
            extractedPrice = confirmedUnitPrice
        )
    }
}

