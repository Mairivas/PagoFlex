package com.equipo22.pagoflex.ui.screens.agent

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.PaymentEntity
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.MetricCard
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.PaymentCard
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.viewmodel.AgentViewModel

@Composable
fun AgentPaymentsScreen(viewModel: AgentViewModel) {
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val commitmentMap = commitments.associateBy { it.id }
    val userMap = users.associateBy { it.id }
    val companyMap = companies.associateBy { it.id }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<PaymentEntity?>(null) }
    val shown = payments.filter { payment ->
        if (query.isBlank()) {
            true
        } else {
            val commitment = commitmentMap[payment.commitmentId]
            val person = userMap[payment.userId]
            val company = companyMap[payment.companyId]
            listOf(
                payment.numeroComprobante,
                payment.medioPago,
                commitment?.concepto ?: "",
                commitment?.folio ?: "",
                person?.nombreCompleto ?: "",
                company?.nombreFantasia ?: ""
            ).any { it.contains(query, ignoreCase = true) }
        }
    }

    selected?.let { payment ->
        val commitment = commitmentMap[payment.commitmentId]
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text("Comprobante ${payment.numeroComprobante}") },
            text = {
                Column {
                    DetailRow("Persona", userMap[payment.userId]?.nombreCompleto ?: "")
                    DetailRow("Empresa", companyMap[payment.companyId]?.nombreFantasia ?: "")
                    DetailRow("Compromiso", commitment?.concepto ?: "")
                    DetailRow("Folio", commitment?.folio ?: "")
                    DetailRow("Fecha", payment.fechaHora)
                    DetailRow("Monto", formatCurrency(payment.montoPagado))
                    DetailRow("Canal", payment.canal)
                    DetailRow("Medio", payment.medioPago)
                    DetailRow("A tiempo", if (payment.aTiempo) "Sí" else "No")
                }
            },
            confirmButton = {
                TextButton(onClick = { selected = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PagoFlexTopBar(title = "Pagos de la plataforma", subtitle = "${payments.size} pagos registrados")
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    value = payments.size.toString(),
                    label = "Pagos",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    value = formatCurrency(payments.sumOf { it.montoPagado }),
                    label = "Recaudado",
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                label = { Text("Buscar por comprobante, persona o empresa") },
                singleLine = true
            )
        }
        if (shown.isEmpty()) {
            EmptyState(
                title = "Sin resultados",
                message = "Prueba con otro comprobante, persona o empresa.",
                modifier = Modifier.padding(top = 30.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp, 10.dp, 16.dp, 28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.id }) { payment ->
                    val commitment = commitmentMap[payment.commitmentId]
                    PaymentCard(
                        payment = payment,
                        concept = commitment?.concepto ?: "Compromiso pagado",
                        companyName = companyMap[payment.companyId]?.nombreFantasia ?: "PagoFlex",
                        onClick = { selected = payment }
                    )
                }
            }
        }
    }
}
