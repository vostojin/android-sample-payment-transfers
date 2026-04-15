package com.fintech.payment.presentation.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette
object FintechColors {
    val Navy900    = Color(0xFF0A1628)
    val Navy800    = Color(0xFF0F2040)
    val Navy700    = Color(0xFF1A3358)
    val Emerald500 = Color(0xFF10B981)
    val Emerald400 = Color(0xFF34D399)
    val Emerald300 = Color(0xFF6EE7B7)
    val Amber500   = Color(0xFFF59E0B)
    val Red500     = Color(0xFFEF4444)
    val Slate100   = Color(0xFFF1F5F9)
    val Slate200   = Color(0xFFE2E8F0)
    val Slate500   = Color(0xFF64748B)
    val Slate700   = Color(0xFF334155)
    val White      = Color(0xFFFFFFFF)
}

private val DarkColorScheme = darkColorScheme(
    primary          = FintechColors.Emerald400,
    onPrimary        = FintechColors.Navy900,
    primaryContainer = FintechColors.Navy700,
    onPrimaryContainer = FintechColors.Emerald300,
    secondary        = FintechColors.Amber500,
    background       = FintechColors.Navy900,
    onBackground     = FintechColors.White,
    surface          = FintechColors.Navy800,
    onSurface        = FintechColors.White,
    surfaceVariant   = FintechColors.Navy700,
    onSurfaceVariant = FintechColors.Slate200,
    error            = FintechColors.Red500,
    outline          = FintechColors.Slate700
)

private val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF047857),
    onPrimary        = FintechColors.White,
    primaryContainer = FintechColors.Emerald300,
    onPrimaryContainer = Color(0xFF064E3B),
    secondary        = Color(0xFFB45309),
    background       = FintechColors.Slate100,
    onBackground     = FintechColors.Navy900,
    surface          = FintechColors.White,
    onSurface        = FintechColors.Navy900,
    surfaceVariant   = FintechColors.Slate200,
    onSurfaceVariant = FintechColors.Slate700,
    error            = FintechColors.Red500,
    outline          = FintechColors.Slate200
)

// ── Typography
val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.3.sp
    )
)

// ── Theme
@Composable
fun PaymentAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
