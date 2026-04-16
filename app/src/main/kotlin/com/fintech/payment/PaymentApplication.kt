package com.fintech.payment

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

@HiltAndroidApp
class PaymentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applyLocale()
    }

    private fun applyLocale() {
        val locale = Locale.getDefault()
        val appLocale = if (locale.language == "sr") {
            val script = locale.script
            val isCyrillic = script == "Cyrl" ||
                (script.isEmpty() && locale.country in listOf("RS", "BA", "ME"))
            if (isCyrillic) {
                LocaleListCompat.forLanguageTags("sr-Cyrl")
            } else {
                LocaleListCompat.forLanguageTags("sr-Latn")
            }
        } else {
            LocaleListCompat.forLanguageTags("en")
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
