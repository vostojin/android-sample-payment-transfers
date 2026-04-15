package com.fintech.payment.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fintech.payment.data.local.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {

    @Query("SELECT * FROM transfers ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Query(
        """SELECT * FROM transfers 
           WHERE sourceAccountId = :accountId OR destinationAccountId = :accountId 
           ORDER BY timestamp DESC"""
    )
    fun getTransfersByAccount(accountId: String): Flow<List<TransferEntity>>

    @Query("SELECT * FROM transfers WHERE id = :id")
    suspend fun getTransfersById(id: String): TransferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity)
}