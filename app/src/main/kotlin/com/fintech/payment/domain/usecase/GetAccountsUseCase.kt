package com.fintech.payment.domain.usecase

import com.fintech.payment.domain.model.Account
import com.fintech.payment.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetAccountsUseCase @Inject constructor(
    private val accountRepository: AccountRepository
) {
    operator fun invoke(): Flow<List<Account>> = accountRepository.getAllAccounts()
}