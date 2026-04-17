package com.sample.paymenttransfer.domain.usecase

import com.sample.paymenttransfer.domain.model.Account
import com.sample.paymenttransfer.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(): Flow<List<Account>> = accountRepository.getAllAccounts()
}