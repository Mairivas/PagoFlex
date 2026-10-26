package com.equipo22.pagoflex.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.ui.components.CommitmentCard
import com.equipo22.pagoflex.ui.components.ComplianceCard
import com.equipo22.pagoflex.ui.components.LoadingScreen
import com.equipo22.pagoflex.ui.components.MetricCard
import com.equipo22.pagoflex.ui.components.PaymentCard
import com.equipo22.pagoflex.ui.components.SectionHeader
import com.equipo22.pagoflex.ui.theme.OrangePrimary
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.utils.nowDateTime
import com.equipo22.pagoflex.utils.parseDate
import com.equipo22.pagoflex.viewmodel.UserViewModel

@Composable
fun UserHomeScreen(
    viewModel: UserViewModel,
    onOpenCommitment: (Long) -> Unit,
    onOpenPay: (Long) -> Unit,
    onOpenReceipt: (Long) -> Unit,
    onOpenRecommend: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val compliance by viewModel.compliance.collectAsStateWithLifecycle()

    val current = user ?: run {
        LoadingScreen()
        return
    }
    val lastUpdate = remember(current.id, commitments, payments) { nowDateTime() }
    val companyMap = companies.associateBy { it.id }
    val payable = commitments.filter { it.estado in CommitmentStatus.payable }
    val next = payable.minByOrNull { parseDate(it.fechaVencimiento)?.time ?: Long.MAX_VALUE }
    val totalToPay = payable.sumOf { it.totalAPagar }
    val inReview = commitments.count { it.estado == CommitmentStatus.EN_REVISION }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(OrangePrimary)
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                            Text(
                                current.nombres.take(1) + current.apellidos.take(1),
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(
                            "Hola, ${current.nombres}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${current.code} · ${current.comuna}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            "Última actualización: $lastUpdate",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
        item {
            ComplianceCard(
                info = compliance,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    value = payable.size.toString(),
                    label = "Por pagar",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    value = formatCurrency(totalToPay),
                    label = "Monto pendiente",
                    modifier = Modifier.weight(1f)
                )
                if (inReview > 0) {
                    MetricCard(
                        value = inReview.toString(),
                        label = "En revisión",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            if (next != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Tu próximo pago", style = MaterialTheme.typography.labelLarge, color = OrangePrimary)
                        Text(
                            next.concepto,
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            companyMap[next.companyId]?.nombreFantasia ?: "PagoFlex",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    formatCurrency(next.totalAPagar),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Vence ${next.fechaVencimiento}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(onClick = { onOpenPay(next.id) }) {
                                Text("Pagar")
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("No tienes pagos pendientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Tus compromisos están al día. Puedes revisar tu historial cuando quieras.",
                            modifier = Modifier.padding(top = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
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
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(onClick = onOpenRecommend, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Recomendar", modifier = Modifier.padding(start = 6.dp))
                }
                FilledTonalButton(
                    onClick = { next?.let { onOpenCommitment(it.id) } },
                    enabled = next != null,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Ver detalle", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
        if (payable.isNotEmpty()) {
            item {
                SectionHeader(
                    "Compromisos por pagar",
                    modifier = Modifier.padding(start = 16.dp, top = 22.dp, bottom = 8.dp)
                )
            }
                items(payable, key = { "commitment-${it.id}" }) { commitment ->
                CommitmentCard(
                    commitment = commitment,
                    companyName = companyMap[commitment.companyId]?.nombreFantasia ?: "PagoFlex",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                    onClick = { onOpenCommitment(commitment.id) }
                )
            }
        }
        if (payments.isNotEmpty()) {
            item {
                SectionHeader(
                    "Últimos pagos",
                    modifier = Modifier.padding(start = 16.dp, top = 22.dp, bottom = 8.dp)
                )
            }
                items(payments.take(3), key = { "payment-${it.id}" }) { payment ->
                val commitment = commitments.firstOrNull { it.id == payment.commitmentId }
                PaymentCard(
                    payment = payment,
                    concept = commitment?.concepto ?: "Compromiso pagado",
                    companyName = companyMap[payment.companyId]?.nombreFantasia ?: "PagoFlex",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                    onClick = { onOpenReceipt(payment.commitmentId) }
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    "Cada pago puntual suma a tus reconocimientos",
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
