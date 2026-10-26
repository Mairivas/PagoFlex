package com.equipo22.pagoflex.ui.screens.executive

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.ui.components.CommitmentCard
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.utils.monthKeyOf
import com.equipo22.pagoflex.utils.monthLabel
import com.equipo22.pagoflex.viewmodel.ExecutiveViewModel

@Composable
fun ExecutiveCommitmentsScreen(
    viewModel: ExecutiveViewModel,
    onOpenCommitment: (Long) -> Unit,
    onRegister: () -> Unit
) {
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val company by viewModel.company.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf("Todos") }
    var periodFilter by remember { mutableStateOf<String?>(null) }
    val filters = listOf("Todos") + CommitmentStatus.all
    val periods = commitments
        .map { monthKeyOf(it.fechaVencimiento) }
        .distinct()
        .sortedDescending()
    val shown = commitments.filter { commitment ->
        (filter == "Todos" || commitment.estado == filter) &&
            (periodFilter == null || monthKeyOf(commitment.fechaVencimiento) == periodFilter)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PagoFlexTopBar(
            title = "Cartera de compromisos",
            subtitle = if (shown.size == commitments.size) {
                "${commitments.size} registros · ${company?.nombreFantasia ?: ""}"
            } else {
                "${shown.size} de ${commitments.size} registros · ${company?.nombreFantasia ?: ""}"
            }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onRegister) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Registrar", modifier = Modifier.padding(start = 4.dp))
            }
        }
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
        Text(
            "Período de vencimiento",
            modifier = Modifier.padding(start = 16.dp, top = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = periodFilter == null,
                    onClick = { periodFilter = null },
                    label = { Text("Todos los meses") }
                )
            }
            items(periods, key = { it }) { period ->
                FilterChip(
                    selected = periodFilter == period,
                    onClick = { periodFilter = period },
                    label = { Text(monthLabel(period)) }
                )
            }
        }
        if (shown.isEmpty()) {
            EmptyState(
                title = "Sin compromisos",
                message = "No hay compromisos que coincidan con los filtros seleccionados.",
                modifier = Modifier.padding(top = 30.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.id }) { commitment ->
                    CommitmentCard(
                        commitment = commitment,
                        companyName = company?.nombreFantasia ?: "Mi empresa",
                        onClick = { onOpenCommitment(commitment.id) }
                    )
                }
            }
        }
    }
}
