package com.fintech.payment.data.repository

import com.fintech.payment.data.local.dao.TransferDao
import com.fintech.payment.data.local.entity.toDomain
import com.fintech.payment.data.local.entity.toEntity
import com.fintech.payment.domain.model.Transfer
import com.fintech.payment.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val transferDao: TransferDao
) : HistoryRepository {

    override fun getAllTransfers(): Flow<List<Transfer>> =
        transferDao.getAllTransfers().map { list -> list.map { it.toDomain() } }

    //override fun getTransfersByAccount(accountId: String): Flow<List<Transfer>> =
    //    transferDao.getTransfersByAccount(accountId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertTransfer(transfer: Transfer) =
        transferDao.insertTransfer(transfer.toEntity())

    //override suspend fun getTransferById(id: String): Transfer? =
    //    transferDao.getTransfersById(id)?.toDomain()
}