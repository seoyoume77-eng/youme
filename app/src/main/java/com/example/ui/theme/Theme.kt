package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val SeniorIvoryColorScheme =
  lightColorScheme(
    primary = MedicationDoneGreen,
    onPrimary = PureWhite,
    primaryContainer = MedicationDoneBadgeBg,
    onPrimaryContainer = MedicationDoneGreenBorder,
    secondary = WaterBluePrimary,
    onSecondary = PureWhite,
    secondaryContainer = WaterBlueLightSurface,
    onSecondaryContainer = WaterBluePrimary,
    background = IvoryBackground,
    onBackground = InkPrimary,
    surface = IvorySurface,
    onSurface = InkPrimary,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = IvoryBorder,
  )

private val SeniorShapes =
  Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun SeniorHealthTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = SeniorIvoryColorScheme,
    typography = AppTypography,
    shapes = SeniorShapes,
    content = content,
  )
}
