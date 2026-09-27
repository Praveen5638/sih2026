package com.example.data

import android.content.Context
import com.example.network.SyncManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ArtisanRepository(
    private val productDao: ProductDao,
    private val draftDao: DraftDao? = null,
    private val syncDao: SyncDao? = null
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)

    suspend fun insertProduct(product: ProductEntity): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) = productDao.updateProduct(product)

    suspend fun deleteProduct(id: Long) = productDao.deleteProduct(id)

    // WRITE LOCALLY FIRST + PERSISTENT OUTBOX SYNC QUEUE
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
}
