package com.equipo22.pagoflex.ui.screens.user

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo22.pagoflex.data.PagoFlexRepository
import com.equipo22.pagoflex.utils.NotificationHelper
import com.equipo22.pagoflex.viewmodel.UserViewModel
import com.equipo22.pagoflex.viewmodel.UserViewModelFactory

private data class UserTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val userTabs = listOf(
    UserTab("home", "Inicio", Icons.Filled.Home),
    UserTab("commitments", "Compromisos", Icons.Filled.ReceiptLong),
    UserTab("history", "Historial", Icons.Filled.History),
    UserTab("profile", "Perfil", Icons.Filled.Person)
)

@Composable
fun UserApp(
    repository: PagoFlexRepository,
    userId: Int,
    onLogout: () -> Unit
) {
    val viewModel: UserViewModel = viewModel(
        key = "user-$userId",
        factory = UserViewModelFactory(repository, userId)
    )
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val context = LocalContext.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()

    LaunchedEffect(user, commitments, companies) {
        val current = user
        if (current != null && companies.isNotEmpty()) {
            NotificationHelper.checkDueAlerts(context, current, commitments, companies)
        }
    }

    Scaffold(
        bottomBar = {
            if (userTabs.any { it.route == route }) {
                NavigationBar {
                    userTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                UserHomeScreen(
                    viewModel = viewModel,
                    onOpenCommitment = { navController.navigate("commitment/$it") },
                    onOpenPay = { navController.navigate("pay/$it") },
                    onOpenReceipt = { navController.navigate("receipt/$it") },
                    onOpenRecommend = { navController.navigate("recommend") }
                )
            }
            composable("commitments") {
                UserCommitmentsScreen(
                    viewModel = viewModel,
                    onOpenCommitment = { navController.navigate("commitment/$it") }
                )
            }
            composable("history") {
                UserHistoryScreen(
                    viewModel = viewModel,
                    onOpenReceipt = { navController.navigate("receipt/$it") }
                )
            }
            composable("profile") {
                UserProfileScreen(
                    viewModel = viewModel,
                    onOpenRecommend = { navController.navigate("recommend") },
                    onLogout = onLogout
                )
            }
            composable(
                route = "commitment/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                UserCommitmentDetailScreen(
                    viewModel = viewModel,
                    commitmentId = id,
                    onBack = { navController.popBackStack() },
                    onOpenPay = { navController.navigate("pay/$id") },
                    onOpenReport = { navController.navigate("report/$id") },
                    onOpenReceipt = { navController.navigate("receipt/$id") }
                )
            }
            composable(
                route = "pay/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                UserPayScreen(
                    viewModel = viewModel,
                    commitmentId = id,
                    onBack = { navController.popBackStack() },
                    onPaid = {
                        navController.navigate("receipt/$id") {
                            popUpTo("commitment/$id") { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = "report/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                UserReportScreen(
                    viewModel = viewModel,
                    commitmentId = id,
                    onBack = { navController.popBackStack() },
                    onReported = { navController.popBackStack() }
                )
            }
            composable(
                route = "receipt/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                UserReceiptScreen(
                    viewModel = viewModel,
                    commitmentId = id,
                    onBack = { navController.popBackStack("home", inclusive = false) }
                )
            }
            composable("recommend") {
                UserRecommendScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
