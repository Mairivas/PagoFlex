package com.equipo22.pagoflex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo22.pagoflex.data.AppDatabase
import com.equipo22.pagoflex.data.PagoFlexRepository
import com.equipo22.pagoflex.data.Roles
import com.equipo22.pagoflex.ui.components.LoadingScreen
import com.equipo22.pagoflex.ui.screens.LandingScreen
import com.equipo22.pagoflex.ui.screens.agent.AgentApp
import com.equipo22.pagoflex.ui.screens.executive.ExecutiveApp
import com.equipo22.pagoflex.ui.screens.user.UserApp
import com.equipo22.pagoflex.ui.theme.PagoFlexTheme
import com.equipo22.pagoflex.utils.NotificationHelper
import com.equipo22.pagoflex.viewmodel.SessionViewModel
import com.equipo22.pagoflex.viewmodel.SessionViewModelFactory

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            PagoFlexTheme {
                PagoFlexRoot()
            }
        }
    }
}

@Composable
private fun PagoFlexRoot() {
    val context = LocalContext.current
    val repository = remember { PagoFlexRepository(AppDatabase.get(context)) }
    val session: SessionViewModel = viewModel(factory = SessionViewModelFactory(repository))
    val ready by session.ready.collectAsStateWithLifecycle()
    val user by session.currentUser.collectAsStateWithLifecycle()
    val users by session.users.collectAsStateWithLifecycle()
    val companies by session.companies.collectAsStateWithLifecycle()

    if (!ready) {
        LoadingScreen()
        return
    }
    val current = user
    if (current == null) {
        LandingScreen(
            users = users,
            companies = companies,
            onSelect = session::select
        )
        return
    }
    when (current.role) {
        Roles.USUARIO_FINAL -> UserApp(
            repository = repository,
            userId = current.id,
            onLogout = session::logout
        )
        Roles.AGENTE -> AgentApp(
            repository = repository,
            agentId = current.id,
            onLogout = session::logout
        )
        Roles.EJECUTIVO -> ExecutiveApp(
            repository = repository,
            executiveId = current.id,
            onLogout = session::logout
        )
        else -> LandingScreen(
            users = users,
            companies = companies,
            onSelect = session::select
        )
    }
}
