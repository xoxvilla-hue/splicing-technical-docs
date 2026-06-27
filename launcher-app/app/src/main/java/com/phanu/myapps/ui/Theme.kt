package com.phanu.myapps.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Gold = Color(0xFFC9A84C)
val GoldLight = Color(0xFFE8C56A)
val GoldDim = Color(0xFF8A7340)
val GoldFaint = Color(0xFF3A3015)
val BgBlack = Color(0xFF07070A)
val SurfaceDark = Color(0xFF0D0C09)
val BorderDark = Color(0xFF1C1808)

@Composable
fun MyAppsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = BgBlack,
            surface = SurfaceDark,
            primary = Gold,
            onPrimary = BgBlack,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}
