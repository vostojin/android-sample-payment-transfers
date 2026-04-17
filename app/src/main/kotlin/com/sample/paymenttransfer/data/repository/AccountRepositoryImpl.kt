package com.sample.paymenttransfer.data.repository

import com.sample.paymenttransfer.data.local.dao.AccountDao
import com.sample.paymenttransfer.data.local.entity.toDomain
import com.sample.paymenttransfer.data.local.entity.toEntity
import com.sample.paymenttransfer.domain.model.Account
import com.sample.paymenttransfer.domain.repository.AccountRepository
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

