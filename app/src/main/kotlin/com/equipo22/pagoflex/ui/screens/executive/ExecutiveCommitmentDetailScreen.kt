package com.equipo22.pagoflex.ui.screens.executive

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.equipo22.pagoflex.data.Catalogs
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.SectionHeader
import com.equipo22.pagoflex.ui.components.StatusChip
import com.equipo22.pagoflex.utils.daysLate
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.utils.maskRut
import com.equipo22.pagoflex.viewmodel.ExecutiveViewModel
import kotlinx.coroutines.launch

@Composable
fun ExecutiveCommitmentDetailScreen(
    viewModel: ExecutiveViewModel,
    commitmentId: Long,
    onBack: () -> Unit
) {
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val commitment = commitments.firstOrNull { it.id == commitmentId }
    val payment = payments.firstOrNull { it.commitmentId == commitmentId }
    val person = users.firstOrNull { it.id == commitment?.userId }
    val commitmentReports = reports.filter { it.commitmentId == commitmentId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var annulling by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf(Catalogs.annulMotives.first()) }
    var comment by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    if (annulling) {
        AlertDialog(
            onDismissRequest = { annulling = false },
            title = { Text("Anular compromiso") },
            text = {
                Column {
                    Text("La anulación conserva el registro y queda asociada a tu usuario.")
                    Catalogs.annulMotives.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = reason == option,
                                onClick = { reason = option }
                            )
                            Text(option)
                        }
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it.take(300) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        label = { Text(if (reason == "Otro") "Explica el motivo" else "Comentario (opcional)") },
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        saving = true
                        viewModel.annulCommitment(commitmentId, reason, comment.ifBlank { null }) { result ->
                            saving = false
                            result.onSuccess {
                                annulling = false
                                comment = ""
                                scope.launch { snackbarHostState.showSnackbar("Compromiso anulado correctamente.") }
                            }.onFailure { error ->
                                scope.launch { snackbarHostState.showSnackbar(error.message ?: "No se pudo anular el compromiso.") }
                            }
                        }
                    },
                    enabled = !saving && (reason != "Otro" || comment.isNotBlank())
                ) {
                    Text("Anular")
                }
            },
            dismissButton = {
                TextButton(onClick = { annulling = false }) {
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
            PagoFlexTopBar(title = commitment?.folio ?: "Compromiso", subtitle = "Cartera de la empresa", onBack = onBack)
            if (commitment == null) {
                EmptyState(
                    title = "Compromiso no encontrado",
                    message = "Vuelve a la cartera para seleccionar un registro válido.",
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
                                Text("Compromiso de pago", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                StatusChip(commitment.estado)
                            }
                            Text(commitment.concepto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                DetailRow("Monto", formatCurrency(commitment.monto))
                                if (commitment.recargoAplicado > 0) {
                                    DetailRow("Recargo", formatCurrency(commitment.recargoAplicado))
                                }
                                DetailRow("Total", formatCurrency(commitment.totalAPagar))
                                DetailRow("Vencimiento", commitment.fechaVencimiento)
                                if (commitment.estado == CommitmentStatus.VENCIDO) {
                                    DetailRow("Días de atraso", daysLate(commitment.fechaVencimiento).toString())
                                }
                                if (commitment.numeroCuota != null && commitment.totalCuotas != null) {
                                    DetailRow("Cuota", "${commitment.numeroCuota} de ${commitment.totalCuotas}")
                                }
                                DetailRow("Registrado", commitment.fechaRegistro)
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
                            SectionHeader("Persona deudora")
                            Text(person?.nombreCompleto ?: "Sin datos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(maskRut(person?.rut ?: ""), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(person?.correo ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(person?.telefono ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(person?.comuna ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (payment != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                SectionHeader("Pago registrado")
                                DetailRow("Comprobante", payment.numeroComprobante)
                                DetailRow("Fecha", payment.fechaHora)
                                DetailRow("Monto", formatCurrency(payment.montoPagado))
                                DetailRow("Canal", payment.canal)
                                DetailRow("Medio", payment.medioPago)
                                DetailRow("A tiempo", if (payment.aTiempo) "Sí" else "No")
                            }
                        }
                    }
                }
                if (commitment.estado == CommitmentStatus.ANULADO) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                SectionHeader("Anulación")
                                DetailRow("Motivo", commitment.anulacionMotivo ?: "")
                                DetailRow("Fecha", commitment.anulacionFecha ?: "")
                                commitment.anulacionComentario?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                val annuler = users.firstOrNull { it.id == commitment.anulacionEjecutivoId }
                                if (annuler != null) {
                                    DetailRow("Ejecutivo", annuler.nombreCompleto)
                                }
                            }
                        }
                    }
                }
                if (commitmentReports.isNotEmpty()) {
                    item {
                        SectionHeader("Reportes asociados")
                    }
                    items(commitmentReports, key = { it.id }) { report ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(report.numero, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    StatusChip(report.estado)
                                }
                                Text(report.motivo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                report.respuesta?.let {
                                    Text("Respuesta: $it", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                if (commitment.estado != CommitmentStatus.PAGADO && commitment.estado != CommitmentStatus.ANULADO) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Anulación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "La anulación conserva el historial. Si el compromiso estaba en revisión, el reporte asociado debe resolverse por un agente.",
                                    modifier = Modifier.padding(top = 4.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                OutlinedButton(
                                    onClick = { annulling = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text("Anular compromiso")
                                }
                            }
                        }
                    }
                }
                if (commitment.estado in CommitmentStatus.payable) {
                    item {
                        Text(
                            "Los pagos se registran desde la app del usuario. Este perfil no recibe pagos ni abonos.",
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
