package com.fintech.payment.domain.model

import java.math.BigDecimal
import java.time.Instant

/**
 * Represents a completed (or failed) fund transfer between two accounts.
 */
data class Transfer(
    val id: String,
    val sourceAccountId: String,
    val destinationAccountId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: TransferStatus,
    val timestamp: Instant,
    val note: String? = null,
    val failureReason: String? = null
)

