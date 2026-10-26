package com.equipo22.pagoflex.ui.screens.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.ui.components.StatusChip
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.utils.maskRut
import com.equipo22.pagoflex.viewmodel.UserViewModel

@Composable
fun UserReceiptScreen(
    viewModel: UserViewModel,
    commitmentId: Long,
    onBack: () -> Unit
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val commitment = commitments.firstOrNull { it.id == commitmentId }
    val payment = payments.firstOrNull { it.commitmentId == commitmentId }
    val company = companies.firstOrNull { it.id == payment?.companyId }
    var bannerVisible by remember { mutableStateOf(false) }
    LaunchedEffect(payment?.id) { bannerVisible = payment != null }

    Column(modifier = Modifier.fillMaxSize()) {
        PagoFlexTopBar(title = "Comprobante de pago", onBack = onBack)
        if (payment == null) {
            EmptyState(
                title = "Sin comprobante",
                message = "Todavía no existe un pago registrado para este compromiso.",
                modifier = Modifier.padding(top = 40.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            item {
                AnimatedVisibility(
                    visible = bannerVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 })
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    "Pago registrado correctamente",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Tu comprobante quedó disponible como respaldo.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("PagoFlex", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Comprobante ${payment.numeroComprobante}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        StatusChip("Pagado")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                        Column {
                            DetailRow("Fecha y hora", payment.fechaHora)
                            DetailRow("Persona", user?.nombreCompleto ?: "")
                            DetailRow("RUT", maskRut(user?.rut ?: ""))
                            DetailRow("Empresa", company?.nombreFantasia ?: "PagoFlex")
                            DetailRow("Compromiso", commitment?.concepto ?: commitmentId.toString())
                            DetailRow("Folio", commitment?.folio ?: "")
                            DetailRow("Medio de pago", payment.medioPago)
                            DetailRow("Canal", payment.canal)
                            DetailRow("Pago a tiempo", if (payment.aTiempo) "Sí" else "No")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                        DetailRow("Monto pagado", formatCurrency(payment.montoPagado))
                        Text(
                            "Este comprobante no puede modificarse. Conserva el número para futuras consultas.",
                            modifier = Modifier.padding(top = 14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            payment.fotoUri?.let { photo ->
                item {
                    Column {
                        Text("Foto del comprobante", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        AsyncImage(
                            model = photo,
                            contentDescription = "Foto adjunta al pago",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            item {
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver al inicio")
                }
            }
        }
        }
    }
}
