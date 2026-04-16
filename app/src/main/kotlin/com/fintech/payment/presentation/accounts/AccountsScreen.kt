package com.fintech.payment.presentation.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fintech.payment.R
import com.fintech.payment.domain.model.Account
import com.fintech.payment.presentation.common.AccountCard
import com.fintech.payment.presentation.common.Screen
import com.fintech.payment.presentation.common.SectionHeader
import com.fintech.payment.presentation.common.formatCurrency
import java.math.BigDecimal
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.screen_title_accounts),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Portfolio summary card
            item {
                val totalBalanceRSD = state.accounts.filter{ it.currency == "RSD" }.fold(BigDecimal.ZERO) { acc, a -> acc + a.balance }
                PortfolioSummaryCard(
                    currency = "RSD",
                    accounts = state.accounts,
                    totalBalance = totalBalanceRSD,
                )

                Spacer(Modifier.height(8.dp))

                val totalBalanceEUR = state.accounts.filter{ it.currency == "EUR" }.fold(BigDecimal.ZERO) { acc, a -> acc + a.balance }
                PortfolioSummaryCard(
                    currency = "EUR",
                    accounts = state.accounts,
                    totalBalance = totalBalanceEUR
                )
                Spacer(Modifier.height(8.dp))
                SectionHeader(stringResource(R.string.accounts_section_all))
            }

            if (state.accounts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.accounts_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = state.accounts,
                    key = { it.id }
                ) { account ->
                    AccountCard(account = account)
                }
            }
        }
    }
}

@Composable
private fun PortfolioSummaryCard(currency: String, accounts: List<Account>, totalBalance: BigDecimal) {

    val accountCount = accounts.filter { it.currency == currency}.size
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.accounts_portfolio_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Row {
                    Text(
                        text = currency,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = totalBalance.formatCurrency(currency),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Text(
                    text = if (accountCount != 1) stringResource(R.string.accounts_active_accounts_other, accountCount) else stringResource(R.string.accounts_active_account_one, accountCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
private fun PortfolioSummaryCardPreview1() {
    val accounts = listOf(Account("", "", "", BigDecimal(1000), currency = "RSD"))
    PortfolioSummaryCard("RSD", accounts, BigDecimal("123456789.10"))
}

@Preview
@Composable
private fun PortfolioSummaryCardPreview2() {
    val accounts = listOf(Account("", "", "", BigDecimal(1000)))
    PortfolioSummaryCard("EUR", accounts, BigDecimal("1234567.89"))
}

