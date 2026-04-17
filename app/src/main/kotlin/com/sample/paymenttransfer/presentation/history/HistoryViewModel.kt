package com.sample.paymenttransfer.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.paymenttransfer.domain.model.Transfer
import com.sample.paymenttransfer.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HistoryUiState(
    val transfers: List<Transfer> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = getHistoryUseCase()
        .map { txns -> HistoryUiState(transfers = txns, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )
}
