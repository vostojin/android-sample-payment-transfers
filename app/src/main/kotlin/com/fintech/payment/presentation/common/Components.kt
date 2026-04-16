package com.fintech.payment.presentation.common

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fintech.payment.R
import com.fintech.payment.domain.model.Account
import com.fintech.payment.domain.model.Transfer
import com.fintech.payment.domain.model.TransferStatus
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dateFormatter =
        DateTimeFormatter.ofPattern("dd. MMM yyyy · HH:mm").withZone(ZoneId.systemDefault())

private val customNumberFormat =
        DecimalFormat("#,##0.00", DecimalFormatSymbols().apply{groupingSeparator = '.'; decimalSeparator = ','})

fun BigDecimal.formatCurrency(currency: String, showSymbol: Boolean = false): String =
        (if (showSymbol) "$currency " else "") + customNumberFormat.format(this)

@Composable
fun AccountCard(
    account: Account,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    showIcon: Boolean = true,
    showBackground: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .border(
                width = if (isSelected) 2.dp else  1.dp,
                color = if (showBackground) borderColor else Color.Transparent,
                shape = shape
            ),
        shape = shape,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            if (showBackground) MaterialTheme.colorScheme.surface else Color.Transparent,
        tonalElevation = if (isSelected) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (showIcon) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.primary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                //Text(
                //    text = account.ownerName,
                //    style = MaterialTheme.typography.bodySmall,
                //    fontWeight = FontWeight.SemiBold
                //)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.id,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!account.isActive) {
                        Spacer(Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(stringResource(R.string.account_badge_inactive), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = account.balance.formatCurrency(account.currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                            if (account.isActive)
                                if (account.balance >= BigDecimal(0))
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = account.currency,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
@Preview
private fun AccountCardPreview1a() {
    AccountCard(Account("ACC-1123", "Veselin Ostojin", "Osnovni račun", BigDecimal("1234567.89"), "EUR", true))
}

@Composable
@Preview
private fun AccountCardPreview1b() {
    AccountCard(Account("ACC-1123", "Veselin Ostojin", "Osnovni račun", BigDecimal("-9876"), "RSD", true))
}

@Composable
@Preview
private fun AccountCardPreview1c() {
    AccountCard(
        account = Account("ACC-4455", "Veselin Ostojin", "Osnovni račun", BigDecimal("567890"), "RSD", true),
        showIcon = false,
        showBackground = false
    )
}

@Composable
@Preview
private fun AccountCardPreview2() {
    AccountCard(Account("ACC-2234", "Stoja Veselinović", "Tekući račun", BigDecimal("98765.43567"), "RSD", false))
}

@Composable
fun TransactionItem(
    transfer: Transfer,
    modifier: Modifier = Modifier,
    accountNamesById: Map<String, String> = emptyMap()
) {
    val statusColor = when (transfer.status) {
        TransferStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        TransferStatus.FAILED  -> MaterialTheme.colorScheme.error
        TransferStatus.PENDING -> MaterialTheme.colorScheme.secondary
    }
    val statusIcon = when (transfer.status) {
        TransferStatus.SUCCESS -> Icons.Default.CheckCircle
        else                   -> Icons.Default.Error
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = transfer.status.name,
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val srcName = accountNamesById[transfer.sourceAccountId] ?: transfer.sourceAccountId
                val dstName = accountNamesById[transfer.destinationAccountId] ?: transfer.destinationAccountId
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = srcName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.MiddleEllipsis
                    )
                    Text(
                        text = stringResource(R.string.transaction_arrow),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = dstName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.MiddleEllipsis
                    )
                }
                Text(
                    text = dateFormatter.format(transfer.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = transfer.note ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.MiddleEllipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {

                Text(
                    text = "-${transfer.amount.formatCurrency(transfer.currency)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = transfer.currency,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
private fun TransactionItemPreview1() {
    val accountNames = emptyMap<String, String>()
    val transfer = Transfer("TR-001", "ACC-123", "ACC-234", BigDecimal("1000"), "RSD", TransferStatus.SUCCESS, Instant.now(),"Prebacivanje sa računa na račun, sa dugačkim opisom koji može biti dugačak i prelamati se u više redova")
    TransactionItem(transfer, accountNamesById = accountNames)
}

@Preview
@Composable
private fun TransactionItemPreview2() {
    val accountNames = mapOf("ACC-123" to "Osnovni račun", "ACC-234" to "Dodatni račun sa dugačkim imenom")
    val transfer = Transfer("TR-001", "ACC-123", "ACC-234", BigDecimal("1000"), "EUR", TransferStatus.PENDING, Instant.now())
    TransactionItem(transfer, accountNamesById = accountNames)
}

@Preview
@Composable
private fun TransactionItemPreview3() {
    val accountNames = mapOf("ACC-234" to "Dodatni račun sa dugačkim imenom")
    val transfer = Transfer("TR-001", "ACC-123", "ACC-234", BigDecimal("10000"), "EUR", TransferStatus.FAILED, Instant.now(),"Opis (opciono) jedan red možda dva, tri, četiri, max 5")
    TransactionItem(transfer, accountNamesById = accountNames)
}

@Composable
fun LoadingOverlay(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.2.sp,
        modifier = modifier.padding(vertical = 8.dp)
    )
}
