package com.sample.paymenttransfer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sample.paymenttransfer.data.local.dao.AccountDao
import com.sample.paymenttransfer.data.local.dao.TransferDao
import com.sample.paymenttransfer.data.local.entity.AccountEntity
import com.sample.paymenttransfer.data.local.entity.TransferEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Database(
    entities = [AccountEntity::class, TransferEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PaymentDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transferDao(): TransferDao

    companion object {
        const val DATABASE_NAME = "payment_database"

        /**
         * RoomDatabase.Callback that seeds demo accounts the first time
         * the database is created. This gives reviewers an immediately
         * usable app without manual setup.
         */
        fun seedCallback(scope: CoroutineScope, accountDao: () -> AccountDao) =
            object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    scope.launch(Dispatchers.IO) {
                        accountDao().insertAll(SEED_ACCOUNTS)
                    }
                }
            }

        private val SEED_ACCOUNTS = listOf(
            AccountEntity(
                id = "ACC-100",
                ownerName = "Petar Petrović",
                description = "Osnovni račun",
                balance = BigDecimal("125000.00"),
                currency = "RSD",
                isActive = true
            ),
            AccountEntity(
                id = "ACC-110",
                ownerName = "Petar Petrović",
                description = "Dodatni račun -  budžet za XXX",
                balance = BigDecimal("3200.50"),
                currency = "RSD",
                isActive = true
            ),
            AccountEntity(
                id = "ACC-120",
                ownerName = "Petar Petrović",
                description = "Dodatni račun - budžet za režije",
                balance = BigDecimal("-8900.75"),
                currency = "RSD",
                isActive = true
            ),
            AccountEntity(
                id = "ACC-130",
                ownerName = "Petar Petrović",
                description = "Dodatni račun - 3 (Zamrznut)",
                balance = BigDecimal("15000.00"),
                currency = "RSD",
                isActive = false
            ),
            AccountEntity(
                id = "ACC-150",
                ownerName = "Petar Petrović",
                description = "Euro račun",
                balance = BigDecimal("10000.00"),
                currency = "EUR",
                isActive = true
            )
        )
    }
}
