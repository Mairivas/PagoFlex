package com.equipo22.pagoflex.ui.screens.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.RecommendationStatus
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.SectionHeader
import com.equipo22.pagoflex.ui.components.StatusChip
import com.equipo22.pagoflex.viewmodel.AgentViewModel
import kotlinx.coroutines.launch

@Composable
fun AgentRecommendationDetailScreen(
    viewModel: AgentViewModel,
    recommendationId: Long,
    onBack: () -> Unit
) {
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val recommendation = recommendations.firstOrNull { it.id == recommendationId }
    val person = users.firstOrNull { it.id == recommendation?.userId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var discarding by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    fun advance(status: String, discardReason: String? = null) {
        val current = recommendation ?: return
        saving = true
        viewModel.advanceRecommendation(current.id, status, discardReason) { result ->
            saving = false
            result.onSuccess {
                discarding = false
                reason = ""
                scope.launch { snackbarHostState.showSnackbar("Referido actualizado a ${it.estado}.") }
            }.onFailure { error ->
                scope.launch { snackbarHostState.showSnackbar(error.message ?: "No se pudo actualizar el referido.") }
            }
        }
    }

    if (discarding) {
        AlertDialog(
            onDismissRequest = { discarding = false },
            title = { Text("Descartar referido") },
            text = {
                Column {
                    Text("Indica el motivo del descarte. Esta acción cierra el referido.")
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it.take(200) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        label = { Text("Motivo") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { advance(RecommendationStatus.DESCARTADA, reason) },
                    enabled = reason.isNotBlank() && !saving
                ) {
                    Text("Descartar")
                }
            },
            dismissButton = {
                TextButton(onClick = { discarding = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            PagoFlexTopBar(title = recommendation?.numero ?: "Referido", subtitle = "Gestión de recomendaciones", onBack = onBack)
            if (recommendation == null) {
                EmptyState(
                    title = "Referido no encontrado",
                    message = "Vuelve a la lista para seleccionar una recomendación válida.",
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Organización recomendada", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                StatusChip(recommendation.estado)
                            }
                            Text(
                                recommendation.nombreOrganizacion,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${recommendation.rubro} · ${recommendation.comuna}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                DetailRow("RUT", recommendation.rutOrganizacion ?: "No informado")
                                DetailRow("Fecha", recommendation.fecha)
                                DetailRow("Recomendada por", person?.nombreCompleto ?: "Usuario")
                                DetailRow("Código", person?.code ?: "")
                            }
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            SectionHeader("Datos de contacto")
                            if (recommendation.autorizaContacto) {
                                DetailRow("Contacto", recommendation.contactoNombre ?: "No informado")
                                DetailRow("Correo", recommendation.contactoCorreo ?: "No informado")
                                DetailRow("Teléfono", recommendation.contactoTelefono ?: "No informado")
                                Text(
                                    "La persona declaró autorizar el contacto.",
                                    modifier = Modifier.padding(top = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    "Sin autorización para usar datos de contacto. Gestiona el avance sin registrar comunicaciones directas.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            SectionHeader("Avance del referido")
                            val step = when (recommendation.estado) {
                                RecommendationStatus.RECIBIDA -> 0.25f
                                RecommendationStatus.CONTACTADA -> 0.5f
                                RecommendationStatus.EN_EVALUACION -> 0.75f
                                RecommendationStatus.SE_UNIO -> 1f
                                else -> 0f
                            }
                            LinearProgressIndicator(
                                progress = { step },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            Text(
                                when (recommendation.estado) {
                                    RecommendationStatus.RECIBIDA -> "Recibida. Aún no se ha contactado a la organización."
                                    RecommendationStatus.CONTACTADA -> "Contactada. Falta evaluar el interés."
                                    RecommendationStatus.EN_EVALUACION -> "En evaluación. La organización está revisando la propuesta."
                                    RecommendationStatus.SE_UNIO -> "La organización se unió a PagoFlex. ¡Referido exitoso!"
                                    else -> "Referido descartado."
                                },
                                modifier = Modifier.padding(top = 10.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            recommendation.motivoDescarte?.let {
                                Text(
                                    "Motivo del descarte: $it",
                                    modifier = Modifier.padding(top = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if (recommendation.estado == RecommendationStatus.SE_UNIO) {
                                Text(
                                    "El usuario recibió el reconocimiento Embajador PagoFlex.",
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                item {
                    when (recommendation.estado) {
                        RecommendationStatus.RECIBIDA -> {
                            Button(
                                onClick = { advance(RecommendationStatus.CONTACTADA) },
                                enabled = !saving,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Marcar como contactada")
                            }
                        }
                        RecommendationStatus.CONTACTADA -> {
                            Button(
                                onClick = { advance(RecommendationStatus.EN_EVALUACION) },
                                enabled = !saving,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Pasar a evaluación")
                            }
                        }
                        RecommendationStatus.EN_EVALUACION -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { advance(RecommendationStatus.SE_UNIO) },
                                    enabled = !saving,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("Marcar como se unió")
                                }
                                OutlinedButton(
                                    onClick = { discarding = true },
                                    enabled = !saving,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Descartar referido")
                                }
                            }
                        }
                        else -> {
                            Text(
                                "Este referido está cerrado y no admite más cambios.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            }
        }
    }
}
