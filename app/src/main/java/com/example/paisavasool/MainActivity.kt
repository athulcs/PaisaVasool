package com.example.paisavasool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.paisavasool.data.database.AppDatabase
import com.example.paisavasool.data.repository.ExpenseRepository
import com.example.paisavasool.ui.screen.AddTransactionScreen
import com.example.paisavasool.ui.screen.AnalyticsScreen
import com.example.paisavasool.ui.screen.DashboardScreen
import com.example.paisavasool.ui.screen.HistoryScreen
import com.example.paisavasool.ui.theme.IndigoPrimary
import com.example.paisavasool.ui.theme.IndigoSecondary
import com.example.paisavasool.ui.theme.PaisaVasoolTheme
import com.example.paisavasool.ui.theme.TextSecondary
import com.example.paisavasool.ui.viewmodel.ExpenseViewModel
import com.example.paisavasool.ui.viewmodel.ExpenseViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ExpenseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database, DAO and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ExpenseRepository(database.transactionDao())

        // Initialize ViewModel using Factory
        val factory = ExpenseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ExpenseViewModel::class.java]

        setContent {
            PaisaVasoolTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "PaisaVasool", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.History)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.BarChart)
    object AddTransaction : Screen("add_transaction", "Add", Icons.Default.Home)
}

@Composable
fun MainAppScreen(viewModel: ExpenseViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.History,
        Screen.Analytics
    )

    val shouldShowBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .height(68.dp)
                            .clip(RoundedCornerShape(34.dp))
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(34.dp))
                    ) {
                        bottomNavItems.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = TextSecondary.copy(alpha = 0.6f),
                                    selectedTextColor = IndigoSecondary,
                                    unselectedTextColor = TextSecondary.copy(alpha = 0.6f),
                                    indicatorColor = IndigoPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(
                bottom = if (shouldShowBottomBar) innerPadding.calculateBottomPadding() else 0.dp
            ),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onAddTransactionClick = {
                        navController.navigate(Screen.AddTransaction.route)
                    },
                    onViewAllClick = {
                        navController.navigate(Screen.History.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onEditClick = { id ->
                        navController.navigate("${Screen.AddTransaction.route}?id=$id")
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onEditClick = { id ->
                        navController.navigate("${Screen.AddTransaction.route}?id=$id")
                    }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = viewModel)
            }
            composable(
                route = "${Screen.AddTransaction.route}?id={id}",
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: -1
                AddTransactionScreen(
                    viewModel = viewModel,
                    transactionId = if (id == -1) null else id,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
