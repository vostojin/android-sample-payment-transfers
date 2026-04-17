package com.sample.paymenttransfer.macrobenchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class StartupBaselineProfile {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    @Test
    fun startup() {
        baselineRule.collect(
            packageName = "com.sample.paymenttransfer"
        ) {
            pressHome()
            val intent = InstrumentationRegistry.getInstrumentation()
                .targetContext
                .packageManager
                .getLaunchIntentForPackage("com.sample.paymenttransfer")!!
            startActivityAndWait(intent)
        }
    }
}