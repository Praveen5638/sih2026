package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DraftDao {

    @Query("SELECT * FROM product_drafts WHERE draftId = :draftId LIMIT 1")
    fun getDraftFlow(draftId: String = "ACTIVE_DRAFT"): Flow<ProductDraftEntity?>

    @Query("SELECT * FROM product_drafts WHERE draftId = :draftId LIMIT 1")
    suspend fun getDraft(draftId: String = "ACTIVE_DRAFT"): ProductDraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: ProductDraftEntity)

    @Query("DELETE FROM product_drafts WHERE draftId = :draftId")
    suspend fun clearDraft(draftId: String = "ACTIVE_DRAFT")
}
