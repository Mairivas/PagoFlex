package com.equipo22.pagoflex.ui.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.ui.components.CommitmentCard
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.viewmodel.UserViewModel

@Composable
fun UserCommitmentsScreen(
    viewModel: UserViewModel,
    onOpenCommitment: (Long) -> Unit
) {
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val companyMap = companies.associateBy { it.id }
    var filter by remember { mutableStateOf("Todos") }
    val filters = listOf("Todos") + CommitmentStatus.all
    val shown = commitments.filter { filter == "Todos" || it.estado == filter }

    Column(modifier = Modifier.fillMaxSize()) {
        PagoFlexTopBar(title = "Mis compromisos", subtitle = "${commitments.size} en total")
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
                title = "Sin compromisos en este filtro",
                message = "Cuando existan compromisos con este estado aparecerán aquí.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.id }) { commitment ->
                    CommitmentCard(
                        commitment = commitment,
                        companyName = companyMap[commitment.companyId]?.nombreFantasia ?: "PagoFlex",
                        onClick = { onOpenCommitment(commitment.id) }
                    )
                }
            }
        }
    }
}
