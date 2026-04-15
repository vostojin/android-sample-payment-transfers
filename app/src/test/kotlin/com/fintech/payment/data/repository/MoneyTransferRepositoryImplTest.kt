package com.fintech.payment.data.repository

import androidx.room.withTransaction
import com.fintech.payment.data.local.PaymentDatabase
import com.fintech.payment.data.local.dao.AccountDao
import com.fintech.payment.data.local.dao.TransferDao
import com.fintech.payment.data.local.entity.AccountEntity
import com.fintech.payment.data.local.entity.TransferEntity
import com.fintech.payment.domain.model.TransferException
import com.fintech.payment.domain.model.TransferStatus
import com.fintech.payment.domain.model.TransferRequest
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class MoneyTransferRepositoryImplTest {

    private lateinit var mDatabase: PaymentDatabase
    private lateinit var mAccountDao: AccountDao
    private lateinit var mTransferDao: TransferDao
    private lateinit var mRepository: MoneyTransferRepositoryImpl

    private val activeSource = AccountEntity(
        id = "ACC-001",
        ownerName = "ownerName ACC-001",
        description = "description ACC-001",
        balance = BigDecimal("500.00"),
        currency = "RSD",
        isActive = true
    )

    private val activeDestination = AccountEntity(
        id = "ACC-002",
        ownerName = "ownerName ACC-002",
        description = "description ACC-002",
        balance = BigDecimal("200.00"),
        currency = "RSD",
        isActive = true
    )

    private val validRequest = TransferRequest(
        sourceAccountId = "ACC-001",
        destinationAccountId = "ACC-002",
        amount = BigDecimal("100.00")
    )

    @Before
    fun setUp() {
        mDatabase = mockk()
        mAccountDao = mockk()
        mTransferDao = mockk()

        // Stub Room's withTransaction extension so the lambda is executed directly
        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Any>()
        coEvery { mDatabase.withTransaction(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }

        mRepository = MoneyTransferRepositoryImpl(mDatabase, mAccountDao, mTransferDao)
    }

    // ── Happy path

    @Test
    fun `executeTransfer debits source and credits destination correctly`() = runTest {
        coEvery { mAccountDao.getAccountById("ACC-001") } returns activeSource
        coEvery { mAccountDao.getAccountById("ACC-002") } returns activeDestination
        coEvery { mAccountDao.updateAccount(any()) } just Runs
        coEvery { mTransferDao.insertTransfer(any()) } just Runs

        val transaction = mRepository.executeTransfer(validRequest)

        assertEquals(TransferStatus.SUCCESS, transaction.status)
        assertEquals(BigDecimal("100.00"), transaction.amount)
        assertEquals("ACC-001", transaction.sourceAccountId)
        assertEquals("ACC-002", transaction.destinationAccountId)

        // Verify balance mutations
        val updatedSlot = mutableListOf<AccountEntity>()
        coVerify(exactly = 2) { mAccountDao.updateAccount(capture(updatedSlot)) }

        val updatedSource = updatedSlot.first { it.id == "ACC-001" }
        val updatedDest   = updatedSlot.first { it.id == "ACC-002" }

        assertEquals(BigDecimal("400.00"), updatedSource.balance)
        assertEquals(BigDecimal("300.00"), updatedDest.balance)

        coVerify(exactly = 1) { mTransferDao.insertTransfer(any()) }
    }

    @Test
    fun `executeTransfer persists transaction record`() = runTest {
        coEvery { mAccountDao.getAccountById("ACC-001") } returns activeSource
        coEvery { mAccountDao.getAccountById("ACC-002") } returns activeDestination
        coEvery { mAccountDao.updateAccount(any()) } just Runs
        val txnSlot = slot<TransferEntity>()
        coEvery { mTransferDao.insertTransfer(capture(txnSlot)) } just Runs

        mRepository.executeTransfer(validRequest)

        assertEquals("SUCCESS", txnSlot.captured.status)
        assertEquals(BigDecimal("100.00"), txnSlot.captured.amount)
    }

    // ── Error cases

    @Test(expected = TransferException.AccountNotFoundException::class)
    fun `executeTransfer throws AccountNotFoundException when source missing`() = runTest {
        coEvery { mAccountDao.getAccountById("ACC-001") } returns null

        mRepository.executeTransfer(validRequest)
    }

    @Test(expected = TransferException.AccountNotFoundException::class)
    fun `executeTransfer throws AccountNotFoundException when destination missing`() = runTest {
        coEvery { mAccountDao.getAccountById("ACC-001") } returns activeSource
        coEvery { mAccountDao.getAccountById("ACC-002") } returns null

        mRepository.executeTransfer(validRequest)
    }

    @Test(expected = TransferException.AccountInactiveException::class)
    fun `executeTransfer throws AccountInactiveException for frozen source`() = runTest {
        val frozenSource = activeSource.copy(isActive = false)
        coEvery { mAccountDao.getAccountById("ACC-001") } returns frozenSource
        coEvery { mAccountDao.getAccountById("ACC-002") } returns activeDestination

        mRepository.executeTransfer(validRequest)
    }

    @Test(expected = TransferException.AccountInactiveException::class)
    fun `executeTransfer throws AccountInactiveException for frozen destination`() = runTest {
        val frozenDestination = activeDestination.copy(isActive = false)
        coEvery { mAccountDao.getAccountById("ACC-001") } returns activeSource
        coEvery { mAccountDao.getAccountById("ACC-002") } returns frozenDestination

        mRepository.executeTransfer(validRequest)
    }

    @Test(expected = TransferException.InsufficientFundsException::class)
    fun `executeTransfer throws InsufficientFundsException when balance too low`() = runTest {
        val poorSource = activeSource.copy(balance = BigDecimal("50.00"))
        coEvery { mAccountDao.getAccountById("ACC-001") } returns poorSource
        coEvery { mAccountDao.getAccountById("ACC-002") } returns activeDestination

        mRepository.executeTransfer(validRequest) // requests 100, only 50 available
    }

    @Test(expected = TransferException.IncompatibleCurrencyException::class)
    fun `executeTransfer throws IncompatibleCurrencyException accounts when currencies differ`() = runTest {
        val wrongDestination = activeDestination.copy(currency = "EUR")
        coEvery { mAccountDao.getAccountById("ACC-001") } returns activeSource
        coEvery { mAccountDao.getAccountById("ACC-002") } returns wrongDestination

        mRepository.executeTransfer(validRequest)
    }

    @Test
    fun `executeTransfer does not update accounts when balance check fails`() = runTest {
        val poorSource = activeSource.copy(balance = BigDecimal("10.00"))
        coEvery { mAccountDao.getAccountById("ACC-001") } returns poorSource
        coEvery { mAccountDao.getAccountById("ACC-002") } returns activeDestination

        try { mRepository.executeTransfer(validRequest) } catch (_: TransferException) {}

        coVerify(exactly = 0) { mAccountDao.updateAccount(any()) }
        coVerify(exactly = 0) { mTransferDao.insertTransfer(any()) }
    }
}
