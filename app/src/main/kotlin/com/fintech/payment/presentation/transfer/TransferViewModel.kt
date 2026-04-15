package com.fintech.payment.presentation.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.payment.domain.model.Account
import com.fintech.payment.domain.model.TransferException
import com.fintech.payment.domain.model.Transfer
import com.fintech.payment.domain.model.TransferRequest
import com.fintech.payment.domain.usecase.GetAccountsUseCase
import com.fintech.payment.domain.usecase.TransferFundsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

// ---- UI State ----

data class TransferUiState(
    val accounts: List<Account> = emptyList(),
    val sourceAccountId: String = "",
    val destinationAccountId: String = "",
    val amount: String = "",
    val note: String = "",
    val isLoading: Boolean = false,
    val amountError: String? = null,
    val sourceError: String? = null,
    val destinationError: String? = null
) {
    val isFormValid: Boolean
        get() = sourceAccountId.isNotBlank()
                && destinationAccountId.isNotBlank()
                && sourceAccountId != destinationAccountId
                && amount.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true
}

sealed class TransferEvent {
    data class Success(val transfer: Transfer) : TransferEvent()
    data class Error(val message: String) : TransferEvent()
}

sealed class TransferAction {
    data class SelectSourceAccount(val id: String) : TransferAction()
    data class SelectDestinationAccount(val id: String) : TransferAction()
    data class EnterAmount(val value: String) : TransferAction()
    data class EnterNote(val value: String) : TransferAction()
    object SubmitTransfer : TransferAction()
    object ResetForm : TransferAction()
}

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferFundsUseCase: TransferFundsUseCase,
    private val getAccountsUseCase: GetAccountsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    // Channel used for one-shot navigation/snackbar events
    private val _events = Channel<TransferEvent>(Channel.BUFFERED)
    val events: Flow<TransferEvent> = _events.receiveAsFlow()

    init {
        // Keep account list fresh via a reactive Flow
        viewModelScope.launch {
            getAccountsUseCase()
                .collect { accounts ->
                    _uiState.update { it.copy(accounts = accounts) }
                }
        }
    }

    fun onAction(action: TransferAction) {
        when (action) {
            is TransferAction.SelectSourceAccount ->
                _uiState.update { it.copy(sourceAccountId = action.id, sourceError = null) }

            is TransferAction.SelectDestinationAccount ->
                _uiState.update { it.copy(destinationAccountId = action.id, destinationError = null) }

            is TransferAction.EnterAmount ->
                _uiState.update { it.copy(amount = action.value, amountError = null) }

            is TransferAction.EnterNote ->
                _uiState.update { it.copy(note = action.value) }

            TransferAction.SubmitTransfer -> submitTransfer()

            TransferAction.ResetForm ->
                _uiState.update {
                    it.copy(
                        sourceAccountId = "",
                        destinationAccountId = "",
                        amount = "",
                        note = "",
                        amountError = null,
                        sourceError = null,
                        destinationError = null
                    )
                }
        }
    }

    private fun submitTransfer() {
        val state = _uiState.value
        if (!validateForm(state)) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val request = TransferRequest(
                sourceAccountId = state.sourceAccountId,
                destinationAccountId = state.destinationAccountId,
                amount = state.amount.toBigDecimal(),
                note = state.note.takeIf { it.isNotBlank() }
            )

            transferFundsUseCase(request).fold(
                onSuccess = { transaction ->
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(TransferEvent.Success(transaction))
                    onAction(TransferAction.ResetForm)
                },
                onFailure = { throwable ->
                    _uiState.update { it.copy(isLoading = false) }
                    val message = when (throwable) {
                        is TransferException.InsufficientFundsException -> "Insufficient funds. Available: ${throwable.available}"
                        is TransferException.AccountNotFoundException -> "Account not found: ${throwable.accountId}"
                        is TransferException.AccountInactiveException -> "Account is frozen: ${throwable.accountId}"
                        is TransferException.SameAccountTransferException -> "Source and destination must differ ${throwable.accountId}"
                        is TransferException.InvalidAmountException -> "Invalid amount: ${throwable.amount}"
                        is TransferException.IncompatibleCurrencyException -> "Source and destination currency must not differ: ${throwable.currencyFrom}, ${throwable.currencyTo}"
                        else -> throwable.message ?: "Transfer failed"
                    }
                    _events.send(TransferEvent.Error(message))
                }
            )
        }
    }

    private fun validateForm(state: TransferUiState): Boolean {
        var valid = true

        if (state.sourceAccountId.isBlank()) {
            _uiState.update { it.copy(sourceError = "Please select a source account") }
            valid = false
        }
        if (state.destinationAccountId.isBlank()) {
            _uiState.update { it.copy(destinationError = "Please select a destination account") }
            valid = false
        }
        if (state.sourceAccountId == state.destinationAccountId && state.sourceAccountId.isNotBlank()) {
            _uiState.update { it.copy(destinationError = "Must differ from source account") }
            valid = false
        }
        val parsedAmount = state.amount.toBigDecimalOrNull()
        if (parsedAmount == null || parsedAmount <= BigDecimal.ZERO) {
            _uiState.update { it.copy(amountError = "Enter a valid amount greater than 0") }
            valid = false
        }
        return valid
    }
}
