package com.sample.paymenttransfer.domain.repository

import com.sample.paymenttransfer.domain.model.Account
import kotlinx.coroutines.flow.Flow

/**
 * Contract for account data operations.
 * The domain layer depends on this interface; the data layer implements it.
 */
interface AccountRepository {
    fun getAllAccounts(): Flow<List<Account>>
    suspend fun getAccountById(id: String): Account?
    suspend fun updateAccount(account: Account)
}