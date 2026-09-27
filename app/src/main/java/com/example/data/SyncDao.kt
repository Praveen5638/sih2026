package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {

    @Query("SELECT * FROM sync_operations WHERE status IN ('PENDING', 'FAILED_RETRYABLE') ORDER BY createdAt ASC")
    fun getPendingOperationsFlow(): Flow<List<SyncOperationEntity>>

    @Query("SELECT * FROM sync_operations WHERE status IN ('PENDING', 'FAILED_RETRYABLE') ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncOperationEntity>

    @Query("SELECT * FROM sync_operations WHERE clientOperationId = :clientOperationId LIMIT 1")
    suspend fun getOperationByClientOperationId(clientOperationId: String): SyncOperationEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOperation(operation: SyncOperationEntity): Long

    @Update
    suspend fun updateOperation(operation: SyncOperationEntity)

    @Query("UPDATE sync_operations SET status = :status, lastAttemptAt = :timestamp, errorMessage = :error WHERE id = :id")
    suspend fun updateOperationStatus(id: String, status: String, timestamp: Long = System.currentTimeMillis(), error: String? = null)

    @Query("DELETE FROM sync_operations WHERE status = 'SYNCED'")
    suspend fun deleteSyncedOperations()
}
