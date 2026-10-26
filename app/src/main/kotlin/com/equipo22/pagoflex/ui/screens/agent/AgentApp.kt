package com.equipo22.pagoflex.ui.screens.agent

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.equipo22.pagoflex.data.PagoFlexRepository
import com.equipo22.pagoflex.viewmodel.AgentViewModel
import com.equipo22.pagoflex.viewmodel.AgentViewModelFactory

private data class AgentTab(val route: String, val label: String, val icon: ImageVector)

private val agentTabs = listOf(
    AgentTab("home", "Inicio", Icons.Filled.Home),
    AgentTab("reports", "Reportes", Icons.Filled.SupportAgent),
    AgentTab("recommendations", "Referidos", Icons.Filled.CardGiftcard),
    AgentTab("payments", "Pagos", Icons.Filled.Payments),
    AgentTab("profile", "Perfil", Icons.Filled.Person)
)

@Composable
fun AgentApp(
    repository: PagoFlexRepository,
    agentId: Int,
    onLogout: () -> Unit
) {
    val viewModel: AgentViewModel = viewModel(
        key = "agent-$agentId",
        factory = AgentViewModelFactory(repository, agentId)
    )
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (agentTabs.any { it.route == route }) {
                NavigationBar {
                    agentTabs.forEach { tab ->
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
                AgentHomeScreen(
                    viewModel = viewModel,
                    onOpenReport = { navController.navigate("report/$it") },
                    onOpenRecommendation = { navController.navigate("recommendation/$it") },
                    onOpenReports = { navController.navigate("reports") }
                )
            }
            composable("reports") {
                AgentReportsScreen(
                    viewModel = viewModel,
                    onOpenReport = { navController.navigate("report/$it") }
                )
            }
            composable("recommendations") {
                AgentRecommendationsScreen(
                    viewModel = viewModel,
                    onOpenRecommendation = { navController.navigate("recommendation/$it") }
                )
            }
            composable("payments") {
                AgentPaymentsScreen(viewModel = viewModel)
            }
            composable("profile") {
                AgentProfileScreen(viewModel = viewModel, onLogout = onLogout)
            }
            composable(
                route = "report/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                AgentReportDetailScreen(
                    viewModel = viewModel,
                    reportId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "recommendation/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                AgentRecommendationDetailScreen(
                    viewModel = viewModel,
                    recommendationId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
