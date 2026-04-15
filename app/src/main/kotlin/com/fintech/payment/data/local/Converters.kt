package com.fintech.payment.data.local

import androidx.room.TypeConverter
import java.math.BigDecimal

/**
 * Room TypeConverters to handle types not natively supported by SQLite.
 */
class Converters {

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun stringToBigDecimal(value: String?): BigDecimal? =
        value?.let { BigDecimal(it) }
}
