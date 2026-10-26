package com.equipo22.pagoflex.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.Catalogs
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.data.ReportEntity
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun UserReportScreen(
    viewModel: UserViewModel,
    commitmentId: Long,
    onBack: () -> Unit,
    onReported: () -> Unit
) {
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val commitment = commitments.firstOrNull { it.id == commitmentId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var motive by remember { mutableStateOf(Catalogs.reportMotives.first()) }
    var comment by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var created by remember { mutableStateOf<ReportEntity?>(null) }

    if (created != null) {
        AlertDialog(
            onDismissRequest = onReported,
            title = { Text("Reporte creado") },
            text = {
                Text(
                    "Registramos el reporte ${created?.numero}. El compromiso quedó en revisión y el equipo de PagoFlex responderá en un plazo de hasta 5 días hábiles."
                )
            },
            confirmButton = {
                TextButton(onClick = onReported) {
                    Text("Entendido")
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
            PagoFlexTopBar(title = "Reportar un problema", subtitle = commitment?.folio, onBack = onBack)
            if (commitment == null || commitment.estado !in CommitmentStatus.payable) {
                EmptyState(
                    title = "No puedes reportar este compromiso",
                    message = "Solo se pueden reportar compromisos pendientes o vencidos que no tengan un reporte abierto.",
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(commitment.concepto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Monto ${formatCurrency(commitment.totalAPagar)} · vence ${commitment.fechaVencimiento}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text("¿Qué ocurrió?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Catalogs.reportMotives.forEach { option ->
                    FilterChip(
                        selected = motive == option,
                        onClick = { motive = option },
                        label = { Text(option) }
                    )
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it.take(400) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (motive == "Otro") "Cuéntanos qué pasó" else "Detalle (opcional)") },
                    minLines = 3,
                    maxLines = 6
                )
                Text(
                    "Al enviar el reporte, el compromiso pasa a revisión y no podrá pagarse hasta que un agente lo resuelva. La respuesta llegará en un plazo de hasta 5 días hábiles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        saving = true
                        viewModel.report(commitment.id, motive, comment.ifBlank { null }) { result ->
                            saving = false
                            result.onSuccess { created = it }.onFailure { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error.message ?: "No se pudo crear el reporte.")
                                }
                            }
                        }
                    },
                    enabled = !saving && (motive != "Otro" || comment.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (saving) "Enviando..." else "Enviar reporte")
                }
            }
            }
        }
    }
}
