package com.sowrobrana.oshudhreminder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern health palette: deep teal + soft mint + warm amber accent.
private val Teal = Color(0xFF0F766E)
private val TealDark = Color(0xFF115E59)
private val Mint = Color(0xFFCCFBF1)
private val MintSoft = Color(0xFFF0FDFA)
private val Amber = Color(0xFFD97706)
private val Ink = Color(0xFF134E4A)
private val Paper = Color(0xFFF8FAF7)

private val OshudhColorScheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = TealDark,
    secondary = Amber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = MintSoft,
    onSurfaceVariant = Color(0xFF475569),
    error = Color(0xFFDC2626),
)

@Composable
fun OshudhReminderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OshudhColorScheme,
        content = content,
    )
}
