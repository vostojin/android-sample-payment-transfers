package com.sample.paymenttransfer.domain.repository

import com.sample.paymenttransfer.domain.model.Transfer
import kotlinx.coroutines.flow.Flow

/**
 * Contract for transfer data operations.
 */
interface HistoryRepository {
    fun getAllTransfers(): Flow<List<Transfer>>
    //fun getTransfersByAccount(accountId: String): Flow<List<Transfer>>
    suspend fun insertTransfer(transfer: Transfer)
    //suspend fun getTransferById(id: String): Transfer?
}