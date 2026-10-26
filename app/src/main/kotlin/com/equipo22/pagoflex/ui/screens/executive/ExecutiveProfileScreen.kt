package com.equipo22.pagoflex.ui.screens.executive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.MetricCard
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.utils.maskRut
import com.equipo22.pagoflex.viewmodel.ExecutiveViewModel

@Composable
fun ExecutiveProfileScreen(
    viewModel: ExecutiveViewModel,
    onLogout: () -> Unit
) {
    val executive by viewModel.executive.collectAsStateWithLifecycle()
    val company by viewModel.company.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val current = executive ?: return
    val active = commitments.count { it.estado != CommitmentStatus.ANULADO }
    val pending = commitments.filter { it.estado in CommitmentStatus.payable }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PagoFlexTopBar(title = "Mi perfil", subtitle = current.nombreCompleto)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(current.nombreCompleto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        current.cargo ?: "Ejecutivo de empresa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        DetailRow("Código", current.code)
                        DetailRow("RUT", maskRut(current.rut))
                        DetailRow("Correo", current.correo)
                        DetailRow("Teléfono", current.telefono)
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Empresa cliente", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(company?.razonSocial ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(company?.nombreFantasia ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        DetailRow("RUT", maskRut(company?.rut ?: ""))
                        DetailRow("Rubro", company?.rubro ?: "")
                        DetailRow("Correo de atención", company?.correoAtencion ?: "")
                        DetailRow("Recargo por atraso", formatCurrency(company?.recargoAtraso ?: 0))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricCard(active.toString(), "Compromisos activos", Modifier.weight(1f))
                MetricCard(pending.size.toString(), "Por cobrar", Modifier.weight(1f))
            }
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Text("Cerrar sesión", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
