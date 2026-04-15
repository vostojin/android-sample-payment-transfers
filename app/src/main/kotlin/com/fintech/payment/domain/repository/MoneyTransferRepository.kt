package com.fintech.payment.domain.repository

import com.fintech.payment.domain.model.Transfer
import com.fintech.payment.domain.model.TransferRequest

/**
 * Contract for the core money transfer operation.
 * Abstracts the atomic fund-transfer business logic from its persistence mechanism.
 */
interface MoneyTransferRepository {
    /**
     * Executes a fund transfer atomically.
     * Returns the recorded [com.fintech.payment.domain.model.Transfer] on success.
     * Throws a domain-specific exception on any failure.
     */
    suspend fun executeTransfer(request: TransferRequest): Transfer
}