package com.sample.paymenttransfer.domain.model

import java.math.BigDecimal

/**
 * Input value object used to initiate a transfer request.
 */
data class TransferRequest(
    val sourceAccountId: String,
    val destinationAccountId: String,
    val amount: BigDecimal,
    val note: String? = null
)