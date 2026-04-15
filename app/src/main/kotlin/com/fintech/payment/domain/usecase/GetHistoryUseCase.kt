package com.fintech.payment.domain.usecase

import com.fintech.payment.domain.model.Transfer
import com.fintech.payment.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<Transfer>> = historyRepository.getAllTransfers()
}