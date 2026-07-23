package com.example.kelimehatirlatici.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Dark mode anahtarı
private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

// Açık renk şeması (Light Theme)
private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1976D2),
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFBBDEFB),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF001F3F),
    secondary = androidx.compose.ui.graphics.Color(0xFF43A047),
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFC8E6C9),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF002106),
    tertiary = androidx.compose.ui.graphics.Color(0xFFFF6F00),
    onTertiary = androidx.compose.ui.graphics.Color.White,
    background = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    onBackground = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFE7E0EC),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF49454F),
    error = androidx.compose.ui.graphics.Color(0xFFB00020),
    onError = androidx.compose.ui.graphics.Color.White
)

// Koyu renk şeması (Dark Theme)
private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF90CAF9),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF003258),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF00497D),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFD1E4FF),
    secondary = androidx.compose.ui.graphics.Color(0xFF81C784),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF003908),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF1B5E20),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFC8E6C9),
    tertiary = androidx.compose.ui.graphics.Color(0xFFFFB74D),
    onTertiary = androidx.compose.ui.graphics.Color(0xFF462A00),
    background = androidx.compose.ui.graphics.Color(0xFF121212),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE6E1E5),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2D2D2D),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFCAC4D0),
    error = androidx.compose.ui.graphics.Color(0xFFCF6679),
    onError = androidx.compose.ui.graphics.Color(0xFF601410)
)

/**
 * Dark mode tercihini DataStore'dan okuyan Flow
 */
fun Context.getDarkModeFlow(): Flow<Boolean> {
    return dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }
}

/**
 * Dark mode tercihini kaydeden fonksiyon
 */
suspend fun Context.setDarkModeEnabled(enabled: Boolean) {
    dataStore.edit { preferences ->
        preferences[DARK_MODE_KEY] = enabled
    }
}

/**
 * Uygulama teması - Dark mode tercihine göre otomatik ayarlanır
 */
@Composable
fun KelimeHatirlaticiTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
