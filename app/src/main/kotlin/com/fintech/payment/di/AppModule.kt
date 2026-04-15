package com.fintech.payment.di

import android.content.Context
import androidx.room.Room
import com.fintech.payment.data.local.PaymentDatabase
import com.fintech.payment.data.local.dao.AccountDao
import com.fintech.payment.data.local.dao.TransferDao
import com.fintech.payment.data.repository.AccountRepositoryImpl
import com.fintech.payment.data.repository.MoneyTransferRepositoryImpl
import com.fintech.payment.data.repository.HistoryRepositoryImpl
import com.fintech.payment.domain.repository.AccountRepository
import com.fintech.payment.domain.repository.MoneyTransferRepository
import com.fintech.payment.domain.repository.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun providePaymentDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): PaymentDatabase {
        return Room.databaseBuilder(
            context,
            PaymentDatabase::class.java,
            PaymentDatabase.DATABASE_NAME
        )
            .addCallback(PaymentDatabase.seedCallback(scope) {
                // Lazy access — database is ready by the time callback fires
                Room.databaseBuilder(context, PaymentDatabase::class.java, PaymentDatabase.DATABASE_NAME)
                    .build().accountDao()
            })
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideAccountDao(db: PaymentDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideTransferDao(db: PaymentDatabase): TransferDao = db.transferDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: MoneyTransferRepositoryImpl): MoneyTransferRepository
}
