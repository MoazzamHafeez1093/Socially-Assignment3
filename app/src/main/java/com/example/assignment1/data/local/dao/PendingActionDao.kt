package com.example.assignment1.data.local.dao

import androidx.room.*
import com.example.assignment1.data.local.entities.PendingActionEntity

@Dao
interface PendingActionDao {
    @Query("SELECT * FROM pending_actions WHERE status = 'pending' ORDER BY createdAt ASC")
    suspend fun getPendingActions(): List<PendingActionEntity>

    @Query("SELECT * FROM pending_actions WHERE id = :id")
    suspend fun getActionById(id: Long): PendingActionEntity?

    @Insert
    suspend fun insertAction(action: PendingActionEntity): Long

    @Update
    suspend fun updateAction(action: PendingActionEntity)

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun deleteAction(id: Long)

    @Query("DELETE FROM pending_actions WHERE status = 'failed' AND retryCount >= maxRetries")
    suspend fun deleteFailedActions()

    @Query("UPDATE pending_actions SET status = :status, lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun updateActionStatus(id: Long, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pending_actions SET retryCount = retryCount + 1, lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun incrementRetryCount(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE pending_actions SET errorMessage = :error WHERE id = :id")
    suspend fun updateActionError(id: Long, error: String)
}
