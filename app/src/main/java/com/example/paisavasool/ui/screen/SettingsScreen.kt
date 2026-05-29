package com.example.paisavasool.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.example.paisavasool.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val isIncomeTrackingEnabled by viewModel.isIncomeTrackingEnabled.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "General",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Income Tracking") },
                supportingContent = { Text("Enable or disable tracking of income entries") },
                leadingContent = {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Switch(
                        checked = isIncomeTrackingEnabled,
                        onCheckedChange = { viewModel.setIncomeTrackingEnabled(it) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.0.0") },
                leadingContent = {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Developed by") },
                supportingContent = { Text("Athul CS") },
                leadingContent = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null
                    )
                }
            )

            ListItem(
                headlineContent = { Text("GitHub Repository") },
                supportingContent = { Text("View source code") },
                leadingContent = {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable {
                    uriHandler.openUri("https://github.com/athulcs/PaisaVasool")
                }
            )
        }
    }
}
