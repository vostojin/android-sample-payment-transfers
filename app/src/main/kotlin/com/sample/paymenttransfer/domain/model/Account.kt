package com.sample.paymenttransfer.domain.model

import java.math.BigDecimal

/**
 * Core domain model representing a bank account.
 * Kept in the domain layer — no Android or Room dependencies.
 */
data class Account(
    val id: String,
    val ownerName: String,
    val description: String,
    val balance: BigDecimal,
    val currency: String = "RSD",
    val isActive: Boolean = true
)