package com.equipo22.pagoflex.ui.screens.executive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.ui.components.CommitmentCard
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.MetricCard
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.SectionHeader
import com.equipo22.pagoflex.ui.components.StatusChip
import com.equipo22.pagoflex.ui.theme.OrangePrimary
import com.equipo22.pagoflex.utils.currentMonthKey
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.utils.formatRate
import com.equipo22.pagoflex.utils.monthKeyOf
import com.equipo22.pagoflex.utils.monthLabel
import com.equipo22.pagoflex.utils.parseDate
import com.equipo22.pagoflex.viewmodel.ExecutiveViewModel

@Composable
fun ExecutiveHomeScreen(
    viewModel: ExecutiveViewModel,
    onOpenCommitment: (Long) -> Unit,
    onOpenCommitments: () -> Unit,
    onRegister: () -> Unit
) {
    val executive by viewModel.executive.collectAsStateWithLifecycle()
    val company by viewModel.company.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val payable = commitments.filter { it.estado in CommitmentStatus.payable }
    val monthKey = currentMonthKey()
    val monthlyCommitments = commitments.filter { monthKeyOf(it.fechaVencimiento) == monthKey }
    val monthlyPayable = monthlyCommitments.filter { it.estado in CommitmentStatus.payable }
    val pendingTotal = monthlyPayable.sumOf { it.totalAPagar }
    val monthlyIds = monthlyCommitments.map { it.id }.toSet()
    val collected = payments.filter { it.commitmentId in monthlyIds }.sumOf { it.montoPagado }
    val considered = monthlyCommitments.count { it.estado != CommitmentStatus.ANULADO }
    val paid = monthlyCommitments.count { it.estado == CommitmentStatus.PAGADO }
    val pending = monthlyCommitments.count { it.estado == CommitmentStatus.PENDIENTE }
    val overdue = monthlyCommitments.count { it.estado == CommitmentStatus.VENCIDO }
    val rate = if (considered > 0) paid * 100.0 / considered else 0.0
    val next = payable.minByOrNull { parseDate(it.fechaVencimiento)?.time ?: Long.MAX_VALUE }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OrangePrimary)
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Column {
                    Text(
                        company?.nombreFantasia ?: "Mi empresa",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Hola, ${executive?.nombres ?: "ejecutivo"} · ${executive?.cargo ?: "Ejecutivo"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        item {
            SectionHeader(
                "Resumen de ${monthLabel(monthKey)}",
                modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp)
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(formatRate(rate), "Tasa de cobranza", Modifier.weight(1f))
                MetricCard(formatCurrency(collected), "Recaudado", Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(paid.toString(), "Pagados", Modifier.weight(1f))
                MetricCard(pending.toString(), "Pendientes", Modifier.weight(1f))
                MetricCard(overdue.toString(), "Vencidos", Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(monthlyCommitments.size.toString(), "Compromisos del mes", Modifier.weight(1f))
                MetricCard(formatCurrency(pendingTotal), "Monto por cobrar", Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionHeader("Acciones rápidas")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(onClick = onRegister, modifier = Modifier.weight(1f)) {
                            Text("Registrar compromiso")
                        }
                        TextButton(onClick = onOpenCommitments) {
                            Text("Ver cartera")
                        }
                    }
                    if (next != null) {
                        Text(
                            "Próximo vencimiento: ${next.folio} · ${next.fechaVencimiento} · ${formatCurrency(next.totalAPagar)}",
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
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
                    .padding(start = 16.dp, end = 8.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Compromisos por cobrar", modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenCommitments) {
                    Text("Ver todos")
                }
            }
        }
        if (payable.isEmpty()) {
            item {
                EmptyState(
                    title = "Cartera al día",
                    message = "No hay compromisos pendientes ni vencidos en tu empresa.",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
                items(payable.take(4), key = { "commitment-${it.id}" }) { commitment ->
                CommitmentCard(
                    commitment = commitment,
                    companyName = company?.nombreFantasia ?: "Mi empresa",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                    onClick = { onOpenCommitment(commitment.id) }
                )
            }
        }
        item {
            SectionHeader(
                "Reportes de mi empresa",
                modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 4.dp)
            )
        }
        if (reports.isEmpty()) {
            item {
                EmptyState(
                    title = "Sin reportes",
                    message = "Los reportes de pago de tus clientes aparecerán aquí.",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
                items(reports.take(3), key = { "report-${it.id}" }) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(report.numero, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            StatusChip(report.estado)
                        }
                        Text(report.motivo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Creado el ${report.fechaCreacion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
