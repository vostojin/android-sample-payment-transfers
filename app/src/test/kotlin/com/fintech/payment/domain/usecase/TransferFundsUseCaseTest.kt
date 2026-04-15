package com.fintech.payment.domain.usecase

import com.fintech.payment.domain.model.TransferException
import com.fintech.payment.domain.model.Transfer
import com.fintech.payment.domain.model.TransferStatus
import com.fintech.payment.domain.model.TransferRequest
import com.fintech.payment.domain.repository.MoneyTransferRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class TransferFundsUseCaseTest {

    private lateinit var mMoneyTransferRepository: MoneyTransferRepository
    private lateinit var useCase: TransferFundsUseCase

    private val mValidRequest = TransferRequest(
        sourceAccountId = "ACC-001",
        destinationAccountId = "ACC-002",
        amount = BigDecimal("100.00")
    )

    private val mSuccessTransfer = Transfer(
        id = UUID.randomUUID().toString(),
        sourceAccountId = "ACC-001",
        destinationAccountId = "ACC-002",
        amount = BigDecimal("100.00"),
        currency = "RSD",
        status = TransferStatus.SUCCESS,
        timestamp = Instant.now()
    )

    @Before
    fun setUp() {
        mMoneyTransferRepository = mockk()
        useCase = TransferFundsUseCase(mMoneyTransferRepository,)
    }

    // ── Happy path

    @Test
    fun `invoke with valid request returns success transaction`() = runTest {
        coEvery { mMoneyTransferRepository.executeTransfer(mValidRequest) } returns mSuccessTransfer

        val result = useCase(mValidRequest)

        assertTrue(result.isSuccess)
        assertEquals(mSuccessTransfer, result.getOrNull())
        coVerify(exactly = 1) { mMoneyTransferRepository.executeTransfer(mValidRequest) }
    }

    // ── Validation failures

    @Test
    fun `invoke with zero amount returns InvalidAmountException`() = runTest {
        val request = mValidRequest.copy(amount = BigDecimal.ZERO)

        val result = useCase(request)

        assertTrue(result.isFailure)
        assertIs<TransferException.InvalidAmountException>(result.exceptionOrNull())
        coVerify(exactly = 0) { mMoneyTransferRepository.executeTransfer(any()) }
    }

    @Test
    fun `invoke with negative amount returns InvalidAmountException`() = runTest {
        val request = mValidRequest.copy(amount = BigDecimal("-50.00"))

        val result = useCase(request)

        assertTrue(result.isFailure)
        assertIs<TransferException.InvalidAmountException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke with same source and destination returns SameAccountTransferException`() = runTest {
        val request = mValidRequest.copy(
            sourceAccountId = "ACC-001",
            destinationAccountId = "ACC-001"
        )

        val result = useCase(request)

        assertTrue(result.isFailure)
        assertIs<TransferException.SameAccountTransferException>(result.exceptionOrNull())
        coVerify(exactly = 0) { mMoneyTransferRepository.executeTransfer(any()) }
    }

    // ── Repository exceptions propagate correctly

    @Test
    fun `invoke returns InsufficientFundsException when repository throws it`() = runTest {
        val exception = TransferException.InsufficientFundsException(
            accountId = "ACC-001",
            available = BigDecimal("50.00"),
            requested = BigDecimal("100.00"),
            currency = "RSD"
        )
        coEvery { mMoneyTransferRepository.executeTransfer(mValidRequest) } throws exception

        val result = useCase(mValidRequest)

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<TransferException.InsufficientFundsException>(error)
        assertEquals("ACC-001", (error as TransferException.InsufficientFundsException).accountId)
    }

    @Test
    fun `invoke returns AccountNotFoundException when source account missing`() = runTest {
        coEvery { mMoneyTransferRepository.executeTransfer(mValidRequest) } throws
                TransferException.AccountNotFoundException("ACC-001")

        val result = useCase(mValidRequest)

        assertTrue(result.isFailure)
        assertIs<TransferException.AccountNotFoundException>(result.exceptionOrNull())
    }

    @Test
    fun `invoke wraps unexpected exceptions in TransferFailedException`() = runTest {
        coEvery { mMoneyTransferRepository.executeTransfer(mValidRequest) } throws
                RuntimeException("DB connection lost")

        val result = useCase(mValidRequest)

        assertTrue(result.isFailure)
        assertIs<TransferException.TransferFailedException>(result.exceptionOrNull())
    }

    // ── Helpers

    private inline fun <reified T> assertIs(value: Any?) {
        assertTrue(
            "Expected ${T::class.simpleName} but got ${value?.javaClass?.simpleName}",
            value is T
        )
    }
}
