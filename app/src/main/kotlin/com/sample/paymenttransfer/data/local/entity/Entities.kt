package com.sample.paymenttransfer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val ownerName: String,
    val description: String,
    val balance: BigDecimal,
    val currency: String,
    val isActive: Boolean
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey val id: String,
    val sourceAccountId: String,
    val destinationAccountId: String,
    val amount: BigDecimal,
    val currency: String,
    val status: String,           // stored as enum name string
    val timestamp: Long,          // epoch millis
    val note: String?,
    val failureReason: String?
)
