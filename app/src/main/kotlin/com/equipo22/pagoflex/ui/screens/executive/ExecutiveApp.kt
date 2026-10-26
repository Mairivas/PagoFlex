package com.equipo22.pagoflex.ui.screens.executive

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.equipo22.pagoflex.viewmodel.ExecutiveViewModel
import com.equipo22.pagoflex.viewmodel.ExecutiveViewModelFactory

private data class ExecutiveTab(val route: String, val label: String, val icon: ImageVector)

private val executiveTabs = listOf(
    ExecutiveTab("home", "Resumen", Icons.Filled.Home),
    ExecutiveTab("commitments", "Compromisos", Icons.Filled.ReceiptLong),
    ExecutiveTab("payments", "Recaudación", Icons.Filled.Payments),
    ExecutiveTab("profile", "Perfil", Icons.Filled.Person)
)

@Composable
fun ExecutiveApp(
    repository: PagoFlexRepository,
    executiveId: Int,
    onLogout: () -> Unit
) {
    val viewModel: ExecutiveViewModel = viewModel(
        key = "executive-$executiveId",
        factory = ExecutiveViewModelFactory(repository, executiveId)
    )
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (executiveTabs.any { it.route == route }) {
                NavigationBar {
                    executiveTabs.forEach { tab ->
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
                ExecutiveHomeScreen(
                    viewModel = viewModel,
                    onOpenCommitment = { navController.navigate("commitment/$it") },
                    onOpenCommitments = { navController.navigate("commitments") },
                    onRegister = { navController.navigate("register") }
                )
            }
            composable("commitments") {
                ExecutiveCommitmentsScreen(
                    viewModel = viewModel,
                    onOpenCommitment = { navController.navigate("commitment/$it") },
                    onRegister = { navController.navigate("register") }
                )
            }
            composable("payments") {
                ExecutivePaymentsScreen(viewModel = viewModel)
            }
            composable("profile") {
                ExecutiveProfileScreen(viewModel = viewModel, onLogout = onLogout)
            }
            composable(
                route = "commitment/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                ExecutiveCommitmentDetailScreen(
                    viewModel = viewModel,
                    commitmentId = id,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("register") {
                ExecutiveRegisterScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() }
                )
            }
        }
    }
}
