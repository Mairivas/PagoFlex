package com.equipo22.pagoflex.ui.screens.agent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.ReportStatus
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.ReportCard
import com.equipo22.pagoflex.utils.businessDaysSince
import com.equipo22.pagoflex.viewmodel.AgentViewModel

@Composable
fun AgentReportsScreen(
    viewModel: AgentViewModel,
    onOpenReport: (Long) -> Unit
) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val userMap = users.associateBy { it.id }
    val companyMap = companies.associateBy { it.id }
    val commitmentMap = commitments.associateBy { it.id }
    var filter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos") + ReportStatus.all
    val shown = reports.filter { filter == "Todos" || it.estado == filter }

    Column(modifier = Modifier.fillMaxSize()) {
        PagoFlexTopBar(title = "Reportes de pago", subtitle = "${reports.size} en la plataforma")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text(option) }
                )
            }
        }
        if (shown.isEmpty()) {
            EmptyState(
                title = "Sin reportes",
                message = "No hay reportes con el estado seleccionado.",
                modifier = Modifier.padding(top = 30.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        personName = userMap[report.userId]?.nombreCompleto ?: "Usuario",
                        commitment = commitmentMap[report.commitmentId],
                        companyName = companyMap[report.companyId]?.nombreFantasia ?: "Empresa",
                        overdue = report.estado == ReportStatus.ABIERTO && businessDaysSince(report.fechaCreacion) > 2,
                        onClick = { onOpenReport(report.id) }
                    )
                }
            }
        }
    }
}
