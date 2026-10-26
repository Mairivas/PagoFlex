package com.equipo22.pagoflex.ui.screens.agent

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
import com.equipo22.pagoflex.data.RecommendationStatus
import com.equipo22.pagoflex.data.ReportStatus
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.MetricCard
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.RecommendationCard
import com.equipo22.pagoflex.ui.components.ReportCard
import com.equipo22.pagoflex.ui.components.SectionHeader
import com.equipo22.pagoflex.ui.theme.OrangePrimary
import com.equipo22.pagoflex.utils.businessDaysSince
import com.equipo22.pagoflex.viewmodel.AgentViewModel

@Composable
fun AgentHomeScreen(
    viewModel: AgentViewModel,
    onOpenReport: (Long) -> Unit,
    onOpenRecommendation: (Long) -> Unit,
    onOpenReports: () -> Unit
) {
    val agent by viewModel.agent.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val userMap = users.associateBy { it.id }
    val companyMap = companies.associateBy { it.id }
    val commitmentMap = commitments.associateBy { it.id }
    val openReports = reports.filter { it.estado == ReportStatus.ABIERTO }
    val inManagement = reports.filter { it.estado == ReportStatus.EN_GESTION }
    val resolved = reports.filter { it.estado == ReportStatus.RESUELTO }
    val pendingReferrals = recommendations.filter { it.estado == RecommendationStatus.RECIBIDA }
    val pendingReports = (openReports + inManagement).sortedBy { it.fechaCreacion }

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
                        "Hola, ${agent?.nombres ?: "agente"}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Panel de gestión PagoFlex · ${agent?.code ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(openReports.size.toString(), "Abiertos", Modifier.weight(1f))
                MetricCard(inManagement.size.toString(), "En gestión", Modifier.weight(1f))
                MetricCard(resolved.size.toString(), "Resueltos", Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(pendingReferrals.size.toString(), "Referidos nuevos", Modifier.weight(1f))
                MetricCard(reports.count { it.agenteId == agent?.id }.toString(), "Tomados por ti", Modifier.weight(1f))
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 18.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Bandeja de reportes", modifier = Modifier.weight(1f))
                TextButton(onClick = onOpenReports) {
                    Text("Ver todos")
                }
            }
        }
        if (pendingReports.isEmpty()) {
            item {
                EmptyState(
                    title = "Sin reportes pendientes",
                    message = "Todos los reportes de la plataforma están resueltos.",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
                items(pendingReports, key = { "report-${it.id}" }) { report ->
                val commitment = commitmentMap[report.commitmentId]
                ReportCard(
                    report = report,
                    personName = userMap[report.userId]?.nombreCompleto ?: "Usuario",
                    commitment = commitment,
                    companyName = companyMap[report.companyId]?.nombreFantasia ?: "Empresa",
                    overdue = report.estado == ReportStatus.ABIERTO && businessDaysSince(report.fechaCreacion) > 2,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                    onClick = { onOpenReport(report.id) }
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Toca un reporte para tomar el caso o registrar su resolución.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            SectionHeader(
                "Referidos por contactar",
                modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 2.dp)
            )
        }
        if (pendingReferrals.isEmpty()) {
            item {
                EmptyState(
                    title = "Sin referidos nuevos",
                    message = "Las recomendaciones de usuarios aparecerán aquí.",
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
                items(pendingReferrals, key = { "recommendation-${it.id}" }) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    personName = userMap[recommendation.userId]?.nombreCompleto ?: "Usuario",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                    onClick = { onOpenRecommendation(recommendation.id) }
                )
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Plazo de respuesta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        "Los reportes deben resolverse en hasta 5 días hábiles. Si necesitas anular un compromiso, la ejecución queda a cargo del ejecutivo de la empresa.",
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

