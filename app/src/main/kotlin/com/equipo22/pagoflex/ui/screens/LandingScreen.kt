package com.equipo22.pagoflex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo22.pagoflex.data.CompanyEntity
import com.equipo22.pagoflex.data.Roles
import com.equipo22.pagoflex.data.UserEntity
import com.equipo22.pagoflex.ui.theme.OrangeDark
import com.equipo22.pagoflex.ui.theme.OrangePrimary

@Composable
fun LandingScreen(
    users: List<UserEntity>,
    companies: List<CompanyEntity>,
    onSelect: (Int) -> Unit
) {
    val finalUsers = users.filter { it.role == Roles.USUARIO_FINAL }
    val agents = users.filter { it.role == Roles.AGENTE }
    val executives = users.filter { it.role == Roles.EJECUTIVO }
    val companyMap = companies.associateBy { it.id }

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
                    .padding(horizontal = 24.dp, vertical = 30.dp)
            ) {
                Column {
                    Text("PagoFlex", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        "Paga flexible, vive tranquilo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        "Elige un perfil de demostración para recorrer la aplicación.",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
        item {
            Text(
                "Perfiles disponibles",
                modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 6.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            RoleSection(
                title = "Usuario final",
                subtitle = "Revisa y paga tus compromisos, reporta problemas y obtén reconocimientos.",
                icon = { Icon(Icons.Filled.Person, contentDescription = null, tint = OrangePrimary) },
                people = finalUsers,
                companyMap = companyMap,
                onSelect = onSelect
            )
        }
        item {
            RoleSection(
                title = "Agente PagoFlex",
                subtitle = "Gestiona reportes de pago y avanza las recomendaciones recibidas.",
                icon = { Icon(Icons.Filled.SupportAgent, contentDescription = null, tint = OrangeDark) },
                people = agents,
                companyMap = companyMap,
                onSelect = onSelect
            )
        }
        item {
            RoleSection(
                title = "Ejecutivo de empresa",
                subtitle = "Consulta la cartera de tu empresa, registra y anula compromisos.",
                icon = { Icon(Icons.Filled.Business, contentDescription = null, tint = OrangeDark) },
                people = executives,
                companyMap = companyMap,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun RoleSection(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    people: List<UserEntity>,
    companyMap: Map<Int, CompanyEntity>,
    onSelect: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Box(modifier = Modifier.size(42.dp), contentAlignment = Alignment.Center) { icon() }
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            people.forEachIndexed { index, person ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.surfaceVariant)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(person.id) }
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(person.nombreCompleto, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        val detail = when (person.role) {
                            Roles.EJECUTIVO -> {
                                val company = companyMap[person.empresaId]?.nombreFantasia ?: "Empresa"
                                "${person.cargo ?: "Ejecutivo"} · $company"
                            }
                            Roles.AGENTE -> "${person.area ?: "Soporte"} · ${person.code}"
                            else -> "${person.code} · ${person.comuna}"
                        }
                        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "Entrar",
                        modifier = Modifier
                            .background(OrangePrimary, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

