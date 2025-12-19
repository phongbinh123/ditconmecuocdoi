package com.example.ffridge.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ffridge.data.model.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // REMOVED: User Profile Section with the "1" display

            Spacer(modifier = Modifier.height(8.dp))

            // Notifications Section
            SettingsSection(title = "Notifications") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Expiry Notifications",
                    description = "Get notified about expiring ingredients",
                    checked = uiState.settings.expiryNotifications,
                    onCheckedChange = { viewModel.updateExpiryNotifications(it) }
                )
            }

            // Appearance Section
            SettingsSection(title = "Appearance") {
                ThemeSelector(
                    selectedTheme = uiState.settings.theme,
                    onThemeSelected = { viewModel.updateTheme(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // UI Scale slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "UI Scale",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = "${(uiState.settings.uiScale * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Slider(
                        value = uiState.settings.uiScale,
                        onValueChange = { viewModel.updateUiScale(it) },
                        valueRange = 0.8f..1.2f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsTextItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    description = "1.0.0"
                )

                SettingsTextItem(
                    icon = Icons.Default.Code,
                    title = "Built with",
                    description = "Kotlin & Jetpack Compose"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            Button(
                onClick = { viewModel.showLogoutConfirmation() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Logout, "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Logout confirmation dialog
    if (uiState.showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLogoutConfirmation() },
            title = { Text("Logout?") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout(onLogout)
                        viewModel.hideLogoutConfirmation()
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideLogoutConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                content = content
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsTextItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ThemeOption(
                theme = AppTheme.FROST,
                selected = selectedTheme == AppTheme.FROST,
                onClick = { onThemeSelected(AppTheme.FROST) },
                modifier = Modifier.weight(1f)
            )

            ThemeOption(
                theme = AppTheme.MIDNIGHT,
                selected = selectedTheme == AppTheme.MIDNIGHT,
                onClick = { onThemeSelected(AppTheme.MIDNIGHT) },
                modifier = Modifier.weight(1f)
            )

            // REMOVED: Sunrise theme option
        }
    }
}

@Composable
private fun ThemeOption(
    theme: AppTheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (theme) {
                        AppTheme.FROST -> "❄️"
                        AppTheme.MIDNIGHT -> "🌙"
                    },
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = theme.name.lowercase().capitalize(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
