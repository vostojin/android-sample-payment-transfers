package com.sample.paymenttransfer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sample.paymenttransfer.presentation.common.AppNavigation
import com.sample.paymenttransfer.presentation.common.PaymentAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaymentAppTheme {
                AppNavigation()
            }
        }
    }
}
