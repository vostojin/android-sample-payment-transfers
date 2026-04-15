package com.fintech.payment.domain.model

import java.math.BigDecimal

sealed class TransferException(message: String) : Exception(message) {

    data class AccountNotFoundException(val accountId: String) :
        TransferException("Account not found: $accountId")

    data class AccountInactiveException(val accountId: String) :
        TransferException("Account is inactive: $accountId")

    data class InvalidAmountException(val amount: BigDecimal) :
        TransferException("Transfer amount must be positive, got: $amount")

    data class SameAccountTransferException(val accountId: String) :
        TransferException("Source and destination accounts must be different")

    data class IncompatibleCurrencyException(val currencyFrom: String, val currencyTo: String) :
        TransferException("Source and destination currencies must be the same")

    data class InsufficientFundsException(
        val accountId: String,
        val available: BigDecimal,
        val requested: BigDecimal,
        val currency: String
    ) : TransferException(
        "Insufficient funds in account $accountId: available $available, requested $requested"
    )

    /** Catch-all for unexpected infrastructure errors. */
    data class TransferFailedException(override val cause: Throwable?) :
        TransferException("Transfer failed due to an unexpected error: ${cause?.message}")
}
