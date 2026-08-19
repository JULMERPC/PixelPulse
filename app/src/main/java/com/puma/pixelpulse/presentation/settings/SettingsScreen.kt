package com.puma.pixelpulse.presentation.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puma.pixelpulse.data.local.UserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
    val defaultVolume by viewModel.defaultVolume.collectAsStateWithLifecycle()
    val defaultSpeed by viewModel.defaultSpeed.collectAsStateWithLifecycle()
    val defaultLoop by viewModel.defaultLoop.collectAsStateWithLifecycle()
    val defaultMuted by viewModel.defaultMuted.collectAsStateWithLifecycle()
    val activeWallpaperName by viewModel.activeWallpaperName.collectAsStateWithLifecycle()
    val showRemoveDialog by viewModel.showRemoveDialog.collectAsStateWithLifecycle()

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelRemoveWallpaper() },
            title = { Text("Quitar wallpaper") },
            text = {
                Text(
                    if (activeWallpaperName != null) {
                        "¿Quitar \"$activeWallpaperName\" como fondo de pantalla? Se restaurará el fondo predeterminado del sistema."
                    } else {
                        "¿Quitar el fondo de pantalla actual? Se restaurará el fondo predeterminado del sistema."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmRemoveWallpaper() }) {
                    Text("Quitar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelRemoveWallpaper() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ajustes",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = "Apariencia") {
                ThemeModeSelector(
                    selected = themeMode,
                    onSelect = { viewModel.setThemeMode(it) }
                )

                SettingsSwitch(
                    title = "Color dinámico",
                    subtitle = "Usar colores del fondo de pantalla (Android 12+)",
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }

            SettingsSection(title = "Wallpaper actual") {
                if (activeWallpaperName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeWallpaperName ?: "",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Fondo de pantalla activo",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.requestRemoveWallpaper() }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Quitar wallpaper",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Text(
                        text = "No hay wallpaper activo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            SettingsSection(title = "Wallpaper por defecto") {
                SettingsSlider(
                    title = "Volumen",
                    value = defaultVolume,
                    valueRange = 0f..1f,
                    onValueChange = { viewModel.setDefaultVolume(it) },
                    valueText = "${(defaultVolume * 100).toInt()}%"
                )

                SettingsSlider(
                    title = "Velocidad",
                    value = defaultSpeed,
                    valueRange = 0.25f..3f,
                    onValueChange = { viewModel.setDefaultSpeed(it) },
                    valueText = "${defaultSpeed}x"
                )

                SettingsSwitch(
                    title = "Loop por defecto",
                    subtitle = "Reproducción continua al aplicar",
                    checked = defaultLoop,
                    onCheckedChange = { viewModel.setDefaultLoop(it) }
                )

                SettingsSwitch(
                    title = "Silenciado por defecto",
                    subtitle = "Sin audio al aplicar nuevo wallpaper",
                    checked = defaultMuted,
                    onCheckedChange = { viewModel.setDefaultMuted(it) }
                )
            }

//            SettingsSection(title = "Wallpaper actual") {
//                if (activeWallpaperName != null) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column(modifier = Modifier.weight(1f)) {
//                            Text(
//                                text = activeWallpaperName ?: "",
//                                style = MaterialTheme.typography.bodyLarge
//                            )
//                            Text(
//                                text = "Fondo de pantalla activo",
//                                style = MaterialTheme.typography.bodySmall,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable { viewModel.requestRemoveWallpaper() }
//                            .padding(vertical = 12.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Filled.Delete,
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.error,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "Quitar wallpaper",
//                            style = MaterialTheme.typography.bodyLarge,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                    }
//                } else {
//                    Text(
//                        text = "No hay wallpaper activo",
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                }
//            }

            SettingsSection(title = "Acerca de") {
                SettingsInfo(
                    title = "PixelPulse",
                    subtitle = "Versión 1.0"
                )

                val context = LocalContext.current
                SettingsInfo(
                    title = "Política de Privacidad",
                    subtitle = "Ver política de privacidad",
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, PrivacyPolicyActivity::class.java))
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun ThemeModeSelector(
    selected: UserPreferences.ThemeMode,
    onSelect: (UserPreferences.ThemeMode) -> Unit
) {
    val options = listOf(
        UserPreferences.ThemeMode.SYSTEM to "Seguir sistema",
        UserPreferences.ThemeMode.LIGHT to "Claro",
        UserPreferences.ThemeMode.DARK to "Oscuro",
        UserPreferences.ThemeMode.AMOLED to "AMOLED"
    )

    Column(modifier = Modifier.selectableGroup()) {
        options.forEach { (mode, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .selectable(
                        selected = selected == mode,
                        onClick = { onSelect(mode) },
                        role = Role.RadioButton
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == mode,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueText: String
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsInfo(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
