package com.equipo22.pagoflex.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.RecommendationCard
import com.equipo22.pagoflex.ui.components.SectionHeader
import com.equipo22.pagoflex.utils.NotificationHelper
import com.equipo22.pagoflex.utils.maskRut
import com.equipo22.pagoflex.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    viewModel: UserViewModel,
    onOpenRecommend: () -> Unit,
    onLogout: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val recognitions by viewModel.recognitions.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val current = user ?: return

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                PagoFlexTopBar(
                    title = "Mi perfil",
                    subtitle = current.nombreCompleto
                )
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        DetailRow("Código", current.code)
                        DetailRow("RUT", maskRut(current.rut))
                        DetailRow("Correo", current.correo)
                        DetailRow("Teléfono", current.telefono)
                        DetailRow("Comuna", current.comuna)
                        DetailRow("Cliente desde", current.fechaIncorporacion)
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text("Avisos de vencimiento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Recibe recordatorios antes del vencimiento.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = current.avisosActivos,
                                onCheckedChange = { checked ->
                                    viewModel.updateAlerts(checked, current.diasAnticipacion) { result ->
                                        result.onFailure {
                                            scope.launch { snackbarHostState.showSnackbar("No se pudo actualizar el aviso.") }
                                        }
                                    }
                                }
                            )
                        }
                        if (current.avisosActivos) {
                            Text(
                                "Avisarme con anticipación",
                                modifier = Modifier.padding(top = 10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 3, 5).forEach { days ->
                                    FilterChip(
                                        selected = current.diasAnticipacion == days,
                                        onClick = {
                                            viewModel.updateAlerts(true, days) { result ->
                                                result.onFailure {
                                                    scope.launch { snackbarHostState.showSnackbar("No se pudo actualizar el aviso.") }
                                                }
                                            }
                                        },
                                        label = { Text("$days día${if (days == 1) "" else "s"}") }
                                    )
                                }
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                if (!NotificationHelper.hasPermission(context)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Activa el permiso de notificaciones para ver el aviso.")
                                    }
                                } else {
                                    NotificationHelper.show(
                                        context,
                                        "Aviso de prueba",
                                        "Así se verán tus recordatorios de vencimiento de PagoFlex.",
                                        99
                                    )
                                    scope.launch { snackbarHostState.showSnackbar("Aviso de prueba enviado.") }
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Enviar aviso de prueba")
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Mis reconocimientos", modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (recognitions.isEmpty()) {
                item {
                    EmptyState(
                        title = "Todavía sin reconocimientos",
                        message = "Paga a tiempo tus compromisos para desbloquear logros.",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(recognitions, key = { "${it.userId}-${it.nombre}" }) { recognition ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(recognition.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(recognition.motivo, style = MaterialTheme.typography.bodySmall)
                            Text(
                                recognition.fechaObtencion,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Mis recomendaciones", modifier = Modifier.weight(1f))
                    Button(onClick = onOpenRecommend) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Text("Nueva", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
            if (recommendations.isEmpty()) {
                item {
                    EmptyState(
                        title = "Aún no recomiendas organizaciones",
                        message = "Invita a una organización y sigue su avance desde aquí.",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(recommendations, key = { it.id }) { recommendation ->
                    RecommendationCard(
                        recommendation = recommendation,
                        personName = "ti",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        onClick = {}
                    )
                }
            }
            item {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Text("Cerrar sesión", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
