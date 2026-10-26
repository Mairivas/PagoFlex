package com.equipo22.pagoflex.ui.screens.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.equipo22.pagoflex.data.ReportStatus
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.SectionHeader
import com.equipo22.pagoflex.ui.components.StatusChip
import com.equipo22.pagoflex.utils.businessDaysSince
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.viewmodel.AgentViewModel
import kotlinx.coroutines.launch

@Composable
fun AgentReportDetailScreen(
    viewModel: AgentViewModel,
    reportId: Long,
    onBack: () -> Unit
) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val report = reports.firstOrNull { it.id == reportId }
    val person = users.firstOrNull { it.id == report?.userId }
    val company = companies.firstOrNull { it.id == report?.companyId }
    val commitment = commitments.firstOrNull { it.id == report?.commitmentId }
    val agent = users.firstOrNull { it.id == report?.agenteId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var response by remember { mutableStateOf("") }
    var annul by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            PagoFlexTopBar(title = report?.numero ?: "Reporte", subtitle = "Gestión de reportes", onBack = onBack)
            if (report == null) {
                EmptyState(
                    title = "Reporte no encontrado",
                    message = "Vuelve a la bandeja para seleccionar un reporte válido.",
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
                                Text("Reporte de pago", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                StatusChip(report.estado)
                            }
                            Text(report.motivo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            report.comentario?.let {
                                Text(
                                    it,
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                DetailRow("Creado", report.fechaCreacion)
                                DetailRow("Días hábiles transcurridos", businessDaysSince(report.fechaCreacion).toString())
                                DetailRow("Tomado por", agent?.nombreCompleto ?: "Sin asignar")
                                if (report.fechaToma != null) DetailRow("Fecha de toma", report.fechaToma ?: "")
                            }
                            val elapsed = businessDaysSince(report.fechaCreacion)
                            if (report.estado == ReportStatus.ABIERTO && elapsed > 2) {
                                Text(
                                    "Este reporte supera el plazo objetivo de 2 días hábiles sin ser tomado.",
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
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
                            SectionHeader("Persona y empresa")
                            Text(person?.nombreCompleto ?: "Usuario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(person?.correo ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(person?.telefono ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                company?.nombreFantasia ?: "Empresa",
                                modifier = Modifier.padding(top = 10.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(company?.correoAtencion ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (commitment != null) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    DetailRow("Compromiso", commitment.folio)
                                    DetailRow("Concepto", commitment.concepto)
                                    DetailRow("Monto", formatCurrency(commitment.totalAPagar))
                                    DetailRow("Vencimiento", commitment.fechaVencimiento)
                                }
                            }
                        }
                    }
                }
                if (report.estado == ReportStatus.ABIERTO) {
                    item {
                        Text(
                            "Toma el reporte para iniciar la gestión. Al tomarlo quedará asignado a tu usuario.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                saving = true
                                viewModel.takeReport(report.id) { result ->
                                    saving = false
                                    result.onSuccess {
                                        scope.launch { snackbarHostState.showSnackbar("Reporte tomado. Ya puedes responder.") }
                                    }.onFailure { error ->
                                        scope.launch { snackbarHostState.showSnackbar(error.message ?: "No se pudo tomar el reporte.") }
                                    }
                                }
                            },
                            enabled = !saving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(50.dp)
                        ) {
                            Text(if (saving) "Tomando..." else "Tomar reporte")
                        }
                    }
                }
                if (report.estado == ReportStatus.EN_GESTION) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                SectionHeader("Respuesta al usuario")
                                OutlinedTextField(
                                    value = response,
                                    onValueChange = { response = it.take(500) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    label = { Text("Explica la resolución") },
                                    minLines = 3,
                                    maxLines = 6
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { annul = !annul }
                                        .padding(top = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(checked = annul, onCheckedChange = { annul = it })
                                    Text(
                                        "Anular el compromiso porque fue pagado directamente a la empresa.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    if (annul) {
                                        "La anulación se registra a nombre del ejecutivo de la empresa. El agente no anula por cuenta propia."
                                    } else {
                                        "Si no corresponde anular, el compromiso vuelve a Pendiente o Vencido según su fecha."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = {
                                        if (response.isBlank()) {
                                            scope.launch { snackbarHostState.showSnackbar("Escribe una respuesta para el usuario.") }
                                        } else {
                                            saving = true
                                            viewModel.resolveReport(report.id, response, annul) { result ->
                                                saving = false
                                                result.onSuccess {
                                                    response = ""
                                                    scope.launch { snackbarHostState.showSnackbar("Reporte resuelto correctamente.") }
                                                }.onFailure { error ->
                                                    scope.launch { snackbarHostState.showSnackbar(error.message ?: "No se pudo resolver el reporte.") }
                                                }
                                            }
                                        }
                                    },
                                    enabled = !saving,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .height(50.dp)
                                ) {
                                    Text(if (saving) "Guardando..." else "Resolver reporte")
                                }
                            }
                        }
                    }
                }
                if (report.estado == ReportStatus.RESUELTO) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                SectionHeader("Resolución")
                                Text(report.respuesta ?: "Sin respuesta registrada.", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Resuelto el ${report.fechaResolucion ?: ""}",
                                    modifier = Modifier.padding(top = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (commitment != null) {
                                    Text(
                                        "Estado del compromiso: ${commitment.estado}",
                                        modifier = Modifier.padding(top = 4.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}
