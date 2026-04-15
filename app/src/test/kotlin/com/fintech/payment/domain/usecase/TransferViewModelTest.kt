package com.fintech.payment.domain.usecase

import app.cash.turbine.test
import com.fintech.payment.domain.model.*
import com.fintech.payment.presentation.transfer.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TransferViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var transferFundsUseCase: TransferFundsUseCase
    private lateinit var getAccountsUseCase: GetAccountsUseCase
    private lateinit var viewModel: TransferViewModel

    private val mockAccounts = listOf(
        Account("ACC-001", "Petar Petrović", "Osnovni", BigDecimal("500"), "RSD"),
        Account("ACC-002", "Petar Petrović", "Dodatni", BigDecimal("200"), "RSD")
    )

    private val mSuccessTransfer = Transfer(
        id = "txn-1",
        sourceAccountId = "ACC-001",
        destinationAccountId = "ACC-002",
        amount = BigDecimal("100"),
        currency = "RSD",
        status = TransferStatus.SUCCESS,
        timestamp = Instant.now()
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transferFundsUseCase = mockk()
        getAccountsUseCase = mockk {
            every { this@mockk.invoke() } returns flowOf(mockAccounts)
        }
        viewModel = TransferViewModel(transferFundsUseCase, getAccountsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `accounts loaded from use case on init`() = runTest {
        advanceUntilIdle()
        assertEquals(mockAccounts, viewModel.uiState.value.accounts)
    }

    @Test
    fun `form is invalid when fields are empty`() {
        assertFalse(viewModel.uiState.value.isFormValid)
    }

    @Test
    fun `form becomes valid when all required fields are filled`() = runTest {
        advanceUntilIdle()
        with(viewModel) {
            onAction(TransferAction.SelectSourceAccount("ACC-001"))
            onAction(TransferAction.SelectDestinationAccount("ACC-002"))
            onAction(TransferAction.EnterAmount("100"))
        }
        assertTrue(viewModel.uiState.value.isFormValid)
    }

    @Test
    fun `submit with valid data emits Success event`() = runTest {
        advanceUntilIdle()
        coEvery { transferFundsUseCase(any()) } returns Result.success(mSuccessTransfer)

        viewModel.events.test {
            viewModel.onAction(TransferAction.SelectSourceAccount("ACC-001"))
            viewModel.onAction(TransferAction.SelectDestinationAccount("ACC-002"))
            viewModel.onAction(TransferAction.EnterAmount("100"))
            viewModel.onAction(TransferAction.SubmitTransfer)
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is TransferEvent.Success)
            assertEquals("txn-1", (event as TransferEvent.Success).transfer.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `submit emits Error event when transfer fails`() = runTest {
        advanceUntilIdle()
        coEvery { transferFundsUseCase(any()) } returns Result.failure(
            TransferException.InsufficientFundsException("ACC-001", BigDecimal("50"), BigDecimal("100"), "RSD")
        )

        viewModel.events.test {
            viewModel.onAction(TransferAction.SelectSourceAccount("ACC-001"))
            viewModel.onAction(TransferAction.SelectDestinationAccount("ACC-002"))
            viewModel.onAction(TransferAction.EnterAmount("100"))
            viewModel.onAction(TransferAction.SubmitTransfer)
            advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is TransferEvent.Error)
            assertTrue((event as TransferEvent.Error).message.contains("Insufficient"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ResetForm clears all fields`() = runTest {
        viewModel.onAction(TransferAction.SelectSourceAccount("ACC-001"))
        viewModel.onAction(TransferAction.EnterAmount("100"))
        viewModel.onAction(TransferAction.ResetForm)

        val state = viewModel.uiState.value
        assertEquals("", state.sourceAccountId)
        assertEquals("", state.amount)
    }

    @Test
    fun `same source and destination marks destination as error`() = runTest {
        advanceUntilIdle()
        coEvery { transferFundsUseCase(any()) } returns Result.failure(
            TransferException.SameAccountTransferException("ACC-001")
        )

        viewModel.onAction(TransferAction.SelectSourceAccount("ACC-001"))
        viewModel.onAction(TransferAction.SelectDestinationAccount("ACC-001"))
        viewModel.onAction(TransferAction.EnterAmount("100"))
        viewModel.onAction(TransferAction.SubmitTransfer)
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.destinationError)
    }
}
