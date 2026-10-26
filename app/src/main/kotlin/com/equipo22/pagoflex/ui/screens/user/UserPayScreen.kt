package com.equipo22.pagoflex.ui.screens.user

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.equipo22.pagoflex.data.Catalogs
import com.equipo22.pagoflex.data.CommitmentStatus
import com.equipo22.pagoflex.ui.components.DetailRow
import com.equipo22.pagoflex.ui.components.EmptyState
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.utils.createImageUri
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun UserPayScreen(
    viewModel: UserViewModel,
    commitmentId: Long,
    onBack: () -> Unit,
    onPaid: () -> Unit
) {
    val commitments by viewModel.commitments.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val commitment = commitments.firstOrNull { it.id == commitmentId }
    val company = companies.firstOrNull { it.id == commitment?.companyId }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var method by remember { mutableStateOf(Catalogs.paymentMethods.first()) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var saving by remember { mutableStateOf(false) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (!success) photoUri = null
    }
    val requestCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createImageUri(context, "comprobante")
            photoUri = uri
            takePicture.launch(uri)
        }
    }

    fun takePhoto() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = createImageUri(context, "comprobante")
            photoUri = uri
            takePicture.launch(uri)
        } else {
            requestCamera.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            PagoFlexTopBar(title = "Pagar compromiso", subtitle = commitment?.folio, onBack = onBack)
            if (commitment == null) {
                EmptyState(
                    title = "Compromiso no encontrado",
                    message = "No podemos preparar el pago porque el compromiso no existe.",
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(commitment.concepto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            company?.nombreFantasia ?: "PagoFlex",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            DetailRow("Monto", formatCurrency(commitment.monto))
                            if (commitment.recargoAplicado > 0) {
                                DetailRow("Recargo por atraso", formatCurrency(commitment.recargoAplicado))
                            }
                            DetailRow("Total a pagar", formatCurrency(commitment.totalAPagar))
                            DetailRow("Vencimiento", commitment.fechaVencimiento)
                        }
                        if (commitment.estado == CommitmentStatus.VENCIDO) {
                            Text(
                                "Este compromiso está vencido. El total incluye el recargo informado por la empresa.",
                                modifier = Modifier.padding(top = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Text(
                    "El pago es total. No se aceptan abonos parciales.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Medio de pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        Catalogs.paymentMethods.forEach { option ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = method == option, onClick = { method = option })
                                Text(option, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
                Text("Foto del comprobante (opcional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Foto del comprobante",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Puedes adjuntar una foto tomada con la cámara.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { takePhoto() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Text("Tomar foto", modifier = Modifier.padding(start = 6.dp))
                    }
                    if (photoUri != null) {
                        OutlinedButton(onClick = { photoUri = null }, modifier = Modifier.weight(1f)) {
                            Text("Quitar foto")
                        }
                    }
                }
                Button(
                    onClick = {
                        saving = true
                        viewModel.pay(
                            commitmentId = commitment.id,
                            method = method,
                            photoUri = photoUri?.toString()
                        ) { result ->
                            saving = false
                            result.onSuccess { onPaid() }.onFailure { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error.message ?: "No se pudo registrar el pago.")
                                }
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (saving) {
                        CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Confirmar pago por ${formatCurrency(commitment.totalAPagar)}")
                    }
                }
                }
            }
        }
    }
}
