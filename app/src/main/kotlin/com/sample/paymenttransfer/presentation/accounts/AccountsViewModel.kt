package com.sample.paymenttransfer.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.paymenttransfer.domain.model.Account
import com.sample.paymenttransfer.domain.usecase.GetAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    getAccountsUseCase: GetAccountsUseCase
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = getAccountsUseCase()
        .map { accounts -> AccountsUiState(accounts = accounts, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState()
        )
}
