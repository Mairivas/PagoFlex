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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.data.ReportEntity
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.SectionHeader
import com.equipo22.pagoflex.ui.components.StatusChip
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.viewmodel.UserViewModel

@Composable
fun UserCommitmentDetailScreen(
    viewModel: UserViewModel,
    commitmentId: Long,
    onBack: () -> Unit,
    onOpenPay: () -> Unit,
    onOpenReport: () -> Unit,
    onOpenReceipt: () -> Unit
) {
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val commitment = commitments.firstOrNull { it.id == commitmentId }
    val company = companies.firstOrNull { it.id == commitment?.companyId }
    val payment = payments.firstOrNull { it.commitmentId == commitmentId }
    val commitmentReports = reports.filter { it.commitmentId == commitmentId }
    var blockReason by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(commitment?.id, commitment?.estado, commitments.size) {
        val current = commitment
        blockReason = if (current == null || current.estado !in CommitmentStatus.payable) {
            null
        } else {
            viewModel.paymentBlockReason(current)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PagoFlexTopBar(title = "Detalle del compromiso", onBack = onBack)
        if (commitment == null) {
            EmptyState(
                title = "Compromiso no encontrado",
                message = "Vuelve a la lista y selecciona un compromiso válido.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(commitment.concepto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    company?.nombreFantasia ?: "PagoFlex",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusChip(commitment.estado)
                        }
                        Text(
                            commitment.folio,
                            modifier = Modifier.padding(top = 10.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            DetailRow("Monto del compromiso", formatCurrency(commitment.monto))
                            if (commitment.recargoAplicado > 0) {
                                DetailRow("Recargo por atraso", formatCurrency(commitment.recargoAplicado))
                            }
                            DetailRow("Total a pagar", formatCurrency(commitment.totalAPagar))
                            DetailRow("Fecha de vencimiento", commitment.fechaVencimiento)
                            if (commitment.numeroCuota != null && commitment.totalCuotas != null) {
                                DetailRow("Cuota", "${commitment.numeroCuota} de ${commitment.totalCuotas}")
                            }
                            DetailRow("Registrado el", commitment.fechaRegistro)
                        }
                    }
                }
            }
            if (commitment.estado == CommitmentStatus.PAGADO && payment != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Pago completado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Comprobante ${payment.numeroComprobante} · ${payment.fechaHora}",
                                modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = onOpenReceipt, modifier = Modifier.padding(top = 12.dp)) {
                                Text("Ver comprobante")
                            }
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
                            Text("Compromiso anulado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                commitment.anulacionMotivo ?: "Anulación registrada por la empresa.",
                                modifier = Modifier.padding(top = 4.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            commitment.anulacionComentario?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            if (commitment.estado in CommitmentStatus.payable) {
                item {
                    if (blockReason != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Hay un compromiso anterior pendiente", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    blockReason ?: "",
                                    modifier = Modifier.padding(top = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "Los pagos se aplican en orden de vencimiento dentro de cada empresa.",
                                    modifier = Modifier.padding(top = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            "El pago es total y se aplica al compromiso seleccionado. No se aceptan abonos parciales.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenPay,
                            enabled = blockReason == null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pagar ${formatCurrency(commitment.totalAPagar)}")
                        }
                        OutlinedButton(onClick = onOpenReport, modifier = Modifier.weight(1f)) {
                            Text("Reportar problema")
                        }
                    }
                }
            }
            if (commitmentReports.isNotEmpty()) {
                item {
                    SectionHeader("Reportes asociados")
                }
                items(commitmentReports, key = { it.id }) { report ->
                    ReportSummary(report)
                }
            }
        }
        }
    }
}

@Composable
private fun ReportSummary(report: ReportEntity) {
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
            report.comentario?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (report.respuesta != null) {
                Text(
                    "Respuesta: ${report.respuesta}",
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "Creado el ${report.fechaCreacion}",
                modifier = Modifier.padding(top = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
