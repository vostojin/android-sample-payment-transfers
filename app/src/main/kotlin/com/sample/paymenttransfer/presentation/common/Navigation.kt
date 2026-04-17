package com.sample.paymenttransfer.presentation.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sample.paymenttransfer.R
import com.sample.paymenttransfer.presentation.accounts.AccountsScreen
import com.sample.paymenttransfer.presentation.history.HistoryScreen
import com.sample.paymenttransfer.presentation.transfer.TransferScreen

sealed class Screen(
    val route: String,
    @StringRes val label: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Accounts : Screen("accounts", R.string.nav_label_accounts, Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance)
    object Transfer : Screen("transfer", R.string.nav_label_transfer, Icons.AutoMirrored.Filled.Send, Icons.AutoMirrored.Outlined.Send)
    object History  : Screen("history",  R.string.nav_label_history,  Icons.Filled.History,       Icons.Outlined.History)
}

private val bottomNavItems = listOf(Screen.Accounts, Screen.Transfer, Screen.History)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy
                        ?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = stringResource(screen.label)
                            )
                        },
                        label = { Text(stringResource(screen.label)) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Accounts.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Accounts.route) { AccountsScreen() }

            composable(Screen.Transfer.route) {
                TransferScreen(
                    onTransferSuccess = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable(Screen.History.route) { HistoryScreen() }
        }
    }
}
