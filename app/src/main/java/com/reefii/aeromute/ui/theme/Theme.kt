package com.reefii.aeromute.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = AeroIndigoPrimary,
    onPrimary = AeroIndigoOnPrimary,
    primaryContainer = AeroIndigoContainer,
    onPrimaryContainer = AeroIndigoOnContainer,
    secondary = AeroCyanSecondary,
    secondaryContainer = AeroCyanContainer,
    onSecondaryContainer = AeroCyanOnContainer,
    background = AeroDarkBackground,
    surface = AeroDarkSurface,
    surfaceVariant = AeroDarkSurfaceVariant,
    onBackground = AeroDarkOnSurface,
    onSurface = AeroDarkOnSurface,
    error = AeroRoseMute
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AeroIndigoPrimary,
    onPrimary = AeroIndigoOnPrimary,
    primaryContainer = AeroIndigoOnContainer,
    onPrimaryContainer = AeroIndigoContainer,
    secondary = AeroCyanSecondary,
    secondaryContainer = AeroCyanOnContainer,
    onSecondaryContainer = AeroCyanContainer,
    background = AeroLightBackground,
    surface = AeroLightSurface,
    surfaceVariant = AeroLightSurfaceVariant,
    onBackground = AeroLightOnSurface,
    onSurface = AeroLightOnSurface,
    error = AeroRoseMute
  )

@Composable
fun AeroMuteTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

