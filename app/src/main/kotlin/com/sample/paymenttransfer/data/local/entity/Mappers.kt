package com.sample.paymenttransfer.data.local.entity

import com.sample.paymenttransfer.domain.model.Account
import com.sample.paymenttransfer.domain.model.Transfer
import com.sample.paymenttransfer.domain.model.TransferStatus
import java.time.Instant

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    ownerName = ownerName,
    description = description,
    balance = balance,
    currency = currency,
    isActive = isActive
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    ownerName = ownerName,
    description = description,
    balance = balance,
    currency = currency,
    isActive = isActive
)

fun TransferEntity.toDomain(): Transfer = Transfer(
    id = id,
    sourceAccountId = sourceAccountId,
    destinationAccountId = destinationAccountId,
    amount = amount,
    currency = currency,
    status = TransferStatus.valueOf(status),
    timestamp = Instant.ofEpochMilli(timestamp),
    note = note,
    failureReason = failureReason
)

fun Transfer.toEntity(): TransferEntity = TransferEntity(
    id = id,
    sourceAccountId = sourceAccountId,
    destinationAccountId = destinationAccountId,
    amount = amount,
    currency = currency,
    status = status.name,
    timestamp = timestamp.toEpochMilli(),
    note = note,
    failureReason = failureReason
)
