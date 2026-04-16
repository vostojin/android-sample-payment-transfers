@file:OptIn(ExperimentalMaterial3Api::class)

package com.fintech.payment.presentation.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintech.payment.domain.model.Account
import com.fintech.payment.presentation.common.AccountCard
import com.fintech.payment.presentation.common.LoadingOverlay
import com.fintech.payment.presentation.common.SectionHeader
import com.fintech.payment.presentation.common.formatCurrency
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal

@Composable
fun TransferScreen(
    onTransferSuccess: (transactionId: String) -> Unit = {},
    viewModel: TransferViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TransferEvent.Success -> {
                    snackbarHostState.showSnackbar(
                        message = "✓ Transfer of ${event.transfer.amount.formatCurrency(event.transfer.currency)} completed",
                        duration = SnackbarDuration.Short
                    )
                    onTransferSuccess(event.transfer.id)
                }
                is TransferEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Transfer Funds",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.imePadding()
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Source account
                item {
                    SectionHeader("From account")
                    Spacer(Modifier.height(6.dp))
                    AccountPickerField(
                        label = "Source account",
                        accounts = state.accounts,
                        selectedId = state.sourceAccountId,
                        errorMessage = state.sourceError,
                        onSelect = { viewModel.onAction(TransferAction.SelectSourceAccount(it)) }
                    )
                }

                // ── Destination account
                item {
                    SectionHeader("To account")
                    Spacer(Modifier.height(6.dp))
                    AccountPickerField(
                        label = "Destination account",
                        accounts = state.accounts.filter { it.id != state.sourceAccountId },
                        selectedId = state.destinationAccountId,
                        errorMessage = state.destinationError,
                        onSelect = { viewModel.onAction(TransferAction.SelectDestinationAccount(it)) }
                    )
                }

                // ── Amount
                item {
                    SectionHeader("Amount")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.amount,
                        onValueChange = { viewModel.onAction(TransferAction.EnterAmount(it)) },
                        placeholder = { Text("0.00") },
                        leadingIcon = { Text("$", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 4.dp)) },
                        leadingIcon = {
                            Icon(Icons.Default.EuroSymbol, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = state.amountError != null,
                        supportingText = state.amountError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                // ── Note (optional)
                item {
                    SectionHeader("Note (optional)")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.note,
                        onValueChange = { viewModel.onAction(TransferAction.EnterNote(it)) },
                        placeholder = { Text("Describe this transaction") },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                }

                // ── Submit
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.onAction(TransferAction.SubmitTransfer) },
                        enabled = state.isFormValid && !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Transfer",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = { viewModel.onAction(TransferAction.ResetForm) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Clear", style = MaterialTheme.typography.labelLarge)
                    }
                }

                item {
                    // Give more scrolling space for on-screen keyboard
                    Spacer(modifier = Modifier.imePadding())
                }
            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}

// ── AccountPickerField

@Composable
private fun AccountPickerField(
    label: String,
    accounts: List<Account>,
    selectedId: String,
    errorMessage: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedAccount?.let { "${it.description} (${it.id})" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            isError = errorMessage != null,
            supportingText = errorMessage?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
            shape = MaterialTheme.shapes.medium
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (accounts.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No accounts available") },
                    onClick = { expanded = false }
                )
            } else {
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = {
                            AccountCard(
                                account = account,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .background(color = Color.Transparent),
                                showIcon = false,
                                isSelected = (account.id == selectedId)
                            )
                            /*
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    account.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Row {
                                    Text(
                                        "${account.id} ${account.currency}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        account.balance.formatCurrency(account.currency),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            */
                        },
                        onClick = {
                            onSelect(account.id)
                            expanded = false
                        },
                        leadingIcon = {
                            if (account.id == selectedId) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun AccountPickerFieldPreview() {
    val accounts = listOf(
        Account("A-001", "ownerName A", "description A", BigDecimal("123456"), "RSD"),
        Account("B-002", "ownerName B", "description B", BigDecimal("78"), "RSD"),
        Account("C-003", "ownerName C", "description C", BigDecimal("91011"), "EUR"),
    )
    AccountPickerField("Label", accounts, "A-001", null, {})
}