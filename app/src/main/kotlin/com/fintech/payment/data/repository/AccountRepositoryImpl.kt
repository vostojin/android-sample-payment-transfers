package com.fintech.payment.data.repository

import com.fintech.payment.data.local.dao.AccountDao
import com.fintech.payment.data.local.entity.toDomain
import com.fintech.payment.data.local.entity.toEntity
import com.fintech.payment.domain.model.Account
import com.fintech.payment.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {

    override fun getAllAccounts(): Flow<List<Account>> =
        accountDao.getAllAccounts().map { list -> list.map { it.toDomain() } }

    override suspend fun getAccountById(id: String): Account? =
        accountDao.getAccountById(id)?.toDomain()

    override suspend fun updateAccount(account: Account) =
        accountDao.updateAccount(account.toEntity())
}

