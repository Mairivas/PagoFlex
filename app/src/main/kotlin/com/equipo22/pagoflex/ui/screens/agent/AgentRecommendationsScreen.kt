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
import com.equipo22.pagoflex.data.RecommendationStatus
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.RecommendationCard
import com.equipo22.pagoflex.viewmodel.AgentViewModel

@Composable
fun AgentRecommendationsScreen(
    viewModel: AgentViewModel,
    onOpenRecommendation: (Long) -> Unit
) {
    val recommendations by viewModel.recommendations.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val userMap = users.associateBy { it.id }
    var filter by remember { mutableStateOf("Todas") }
    val filters = listOf("Todas") + RecommendationStatus.all
    val shown = recommendations.filter { filter == "Todas" || it.estado == filter }

    Column(modifier = Modifier.fillMaxSize()) {
        PagoFlexTopBar(title = "Referidos", subtitle = "${recommendations.size} recomendaciones recibidas")
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
                title = "Sin recomendaciones",
                message = "No hay referidos con el estado seleccionado.",
                modifier = Modifier.padding(top = 30.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(shown, key = { it.id }) { recommendation ->
                    RecommendationCard(
                        recommendation = recommendation,
                        personName = userMap[recommendation.userId]?.nombreCompleto ?: "Usuario",
                        onClick = { onOpenRecommendation(recommendation.id) }
                    )
                }
            }
        }
    }
}
