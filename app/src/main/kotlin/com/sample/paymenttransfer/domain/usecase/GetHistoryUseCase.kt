package com.sample.paymenttransfer.domain.usecase

import com.sample.paymenttransfer.domain.model.Transfer
import com.sample.paymenttransfer.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class GetHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(): Flow<List<Transfer>> = historyRepository.getAllTransfers()
}