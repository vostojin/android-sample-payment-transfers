package com.fintech.payment.data.repository

import androidx.room.withTransaction
import com.fintech.payment.data.local.PaymentDatabase
import com.fintech.payment.data.local.dao.AccountDao
import com.fintech.payment.data.local.dao.TransferDao
import com.fintech.payment.data.local.entity.toDomain
import com.fintech.payment.data.local.entity.toEntity
import com.fintech.payment.domain.model.TransferException
import com.fintech.payment.domain.model.Transfer
import com.fintech.payment.domain.model.TransferStatus
import com.fintech.payment.domain.model.TransferRequest
import com.fintech.payment.domain.repository.MoneyTransferRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implements the atomic fund transfer:
 *
 *  1. Load both accounts — throw [TransferException.AccountNotFoundException] if missing
 *  2. Verify both are active — throw [TransferException.AccountInactiveException] otherwise
 *  3. Verify source balance ≥ amount — throw [TransferException.InsufficientFundsException] otherwise
 *  4. Debit source, credit destination
 *  5. Persist both account updates and the transfer record atomically via Room's
 *     `withTransaction` (currently done in a single IO dispatcher context; a full
 *     Room `withTransaction` block can be added when a Database reference is injected)
 *
 * All operations run on [Dispatchers.IO] to stay off the main thread.
 */
@Singleton
class MoneyTransferRepositoryImpl @Inject constructor(
    private val database: PaymentDatabase,
    private val accountDao: AccountDao,
    private val transferDao: TransferDao
) : MoneyTransferRepository {

    override suspend fun executeTransfer(request: TransferRequest): Transfer =
        withContext(Dispatchers.IO) {

            // validation
            val sourceEntity = accountDao.getAccountById(request.sourceAccountId)
                ?: throw TransferException.AccountNotFoundException(request.sourceAccountId)

            val destinationEntity = accountDao.getAccountById(request.destinationAccountId)
                ?: throw TransferException.AccountNotFoundException(request.destinationAccountId)

            val source = sourceEntity.toDomain()
            val destination = destinationEntity.toDomain()

            if (!source.isActive) {
                throw TransferException.AccountInactiveException(source.id)
            }

            if (!destination.isActive) {
                throw TransferException.AccountInactiveException(destination.id)
            }

            if (source.balance < request.amount) {
                throw TransferException.InsufficientFundsException(
                    accountId = source.id,
                    available = source.balance,
                    requested = request.amount,
                    currency = source.currency
                )
            }

            if (source.currency != destination.currency) {
                throw TransferException.IncompatibleCurrencyException(
                    currencyFrom = source.currency,
                    currencyTo = destination.currency
                )
            }

            // prepare transfer
            val updatedSource = source.copy(balance = source.balance - request.amount)
            val updatedDestination = destination.copy(balance = destination.balance + request.amount)

            // persist atomically using a Room transaction
            database.withTransaction {
                accountDao.updateAccount(updatedSource.toEntity())
                accountDao.updateAccount(updatedDestination.toEntity())

                val transfer = Transfer(
                    id = UUID.randomUUID().toString(),
                    sourceAccountId = source.id,
                    destinationAccountId = destination.id,
                    amount = request.amount,
                    currency = source.currency,
                    status = TransferStatus.SUCCESS,
                    timestamp = Instant.now(),
                    note = request.note
                )

                transferDao.insertTransfer(transfer.toEntity())
                transfer
            }
        }
}
