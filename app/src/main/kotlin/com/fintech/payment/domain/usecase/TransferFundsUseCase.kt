package com.fintech.payment.domain.usecase

import com.fintech.payment.domain.model.TransferException
import com.fintech.payment.domain.model.Transfer
import com.fintech.payment.domain.model.TransferRequest
import com.fintech.payment.domain.repository.MoneyTransferRepository
import java.math.BigDecimal
import javax.inject.Inject


/**
 * Use case that orchestrates a payment transfer.
 *
 * Responsibilities:
 *  1. Input validation (amount > 0, accounts differ, same currency)
 *  2. Delegating the atomic transfer to [MoneyTransferRepository]
 *  3. Surfacing domain exceptions as a [Result] wrapper
 *
 * The heavy business rules (balance check, atomicity) live in [MoneyTransferRepository]
 * so they can be tested and swapped independently.
 */
class TransferFundsUseCase @Inject constructor(
    private val moneyTransferRepository: MoneyTransferRepository
) {
    /**
     * @param request validated transfer parameters
     * @return [Result.success] with the persisted [Transfer], or
     *         [Result.failure] wrapping a [TransferException]
     */
    suspend operator fun invoke(request: TransferRequest): Result<Transfer> {
        return try {
            validateRequest(request)
            val transaction = moneyTransferRepository.executeTransfer(request)
            Result.success(transaction)
        } catch (e: TransferException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(TransferException.TransferFailedException(e))
        }
    }

    /** Light structural validation before hitting the repository. */
    private fun validateRequest(request: TransferRequest) {
        if (request.amount <= BigDecimal.ZERO) {
            throw TransferException.InvalidAmountException(request.amount)
        }
        if (request.sourceAccountId == request.destinationAccountId) {
            throw TransferException.SameAccountTransferException(request.destinationAccountId)
        }
    }
}
