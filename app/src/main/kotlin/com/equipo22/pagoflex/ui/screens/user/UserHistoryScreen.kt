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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.MetricCard
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.PaymentCard
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.utils.monthKeyOf
import com.equipo22.pagoflex.utils.monthLabel
import com.equipo22.pagoflex.viewmodel.UserViewModel

@Composable
fun UserHistoryScreen(
    viewModel: UserViewModel,
    onOpenReceipt: (Long) -> Unit
) {
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val companyMap = companies.associateBy { it.id }
    val commitmentMap = commitments.associateBy { it.id }
    val companyFilters = payments
        .mapNotNull { payment ->
            companyMap[payment.companyId]?.let { it.id to it.nombreFantasia }
        }
        .distinctBy { it.first }
        .sortedBy { it.second }
    val periodFilters = payments
        .map { monthKeyOf(it.fechaHora) }
        .distinct()
        .sortedDescending()
    var companyFilter by remember { mutableStateOf<Int?>(null) }
    var periodFilter by remember { mutableStateOf<String?>(null) }
    val filteredPayments = payments.filter { payment ->
        (companyFilter == null || payment.companyId == companyFilter) &&
            (periodFilter == null || monthKeyOf(payment.fechaHora) == periodFilter)
    }
    val groups = filteredPayments.groupBy { monthKeyOf(it.fechaHora) }.toList()
    val total = filteredPayments.sumOf { it.montoPagado }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PagoFlexTopBar(
            title = "Historial de pagos",
            subtitle = if (filteredPayments.size == payments.size) {
                "${payments.size} pagos registrados"
            } else {
                "${filteredPayments.size} de ${payments.size} pagos"
            }
        )
        if (payments.isEmpty()) {
            EmptyState(
                title = "Aún no tienes pagos",
                message = "Cuando pagues un compromiso verás aquí su comprobante.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            Text(
                "Empresa",
                modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = companyFilter == null,
                        onClick = { companyFilter = null },
                        label = { Text("Todas") }
                    )
                }
                items(companyFilters, key = { it.first }) { option ->
                    FilterChip(
                        selected = companyFilter == option.first,
                        onClick = { companyFilter = option.first },
                        label = { Text(option.second) }
                    )
                }
            }
            Text(
                "Período",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 2.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = periodFilter == null,
                        onClick = { periodFilter = null },
                        label = { Text("Todos") }
                    )
                }
                items(periodFilters, key = { it }) { period ->
                    FilterChip(
                        selected = periodFilter == period,
                        onClick = { periodFilter = period },
                        label = { Text(monthLabel(period)) }
                    )
                }
            }
            if (filteredPayments.isEmpty()) {
                EmptyState(
                    title = "Sin pagos",
                    message = "No hay pagos que coincidan con los filtros seleccionados.",
                    modifier = Modifier.padding(top = 24.dp)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 28.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(value = filteredPayments.size.toString(), label = "Pagos", modifier = Modifier.weight(1f))
                            MetricCard(value = formatCurrency(total), label = "Total pagado", modifier = Modifier.weight(1f))
                        }
                    }
                    groups.forEach { (month, monthPayments) ->
                        item(key = "month-$month") {
                            Text(
                                monthLabel(month),
                                modifier = Modifier.padding(top = 14.dp, bottom = 2.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(monthPayments, key = { it.id }) { payment ->
                            val commitment = commitmentMap[payment.commitmentId]
                            PaymentCard(
                                payment = payment,
                                concept = commitment?.concepto ?: "Compromiso pagado",
                                companyName = companyMap[payment.companyId]?.nombreFantasia ?: "PagoFlex",
                                onClick = { onOpenReceipt(payment.commitmentId) }
                            )
                        }
                    }
                    item {
                        Text(
                            "Los comprobantes son inmutables y se conservan como respaldo del pago.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
