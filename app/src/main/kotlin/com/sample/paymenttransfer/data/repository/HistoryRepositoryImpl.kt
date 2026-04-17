package com.sample.paymenttransfer.data.repository

import com.sample.paymenttransfer.data.local.dao.TransferDao
import com.sample.paymenttransfer.data.local.entity.toDomain
import com.sample.paymenttransfer.data.local.entity.toEntity
import com.sample.paymenttransfer.domain.model.Transfer
import com.sample.paymenttransfer.domain.repository.HistoryRepository
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