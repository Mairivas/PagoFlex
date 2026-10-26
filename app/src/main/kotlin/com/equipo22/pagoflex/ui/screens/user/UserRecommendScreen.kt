package com.equipo22.pagoflex.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo22.pagoflex.data.Catalogs
import com.equipo22.pagoflex.data.RecommendationEntity
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.utils.isValidRut
import com.equipo22.pagoflex.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRecommendScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var rubro by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var contactName by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var authorized by remember { mutableStateOf(false) }
    var rubroExpanded by remember { mutableStateOf(false) }
    var comunaExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var created by remember { mutableStateOf<RecommendationEntity?>(null) }
    val rutValid = rut.isBlank() || isValidRut(rut)

    if (created != null) {
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Recomendación registrada") },
            text = {
                Text(
                    "Guardamos la recomendación ${created?.numero}. Un agente de PagoFlex contactará a la organización y podrás seguir su avance en tu perfil."
                )
            },
            confirmButton = {
                TextButton(onClick = onBack) {
                    Text("Volver")
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
            PagoFlexTopBar(
                title = "Recomendar PagoFlex",
                subtitle = "Máximo 5 recomendaciones por mes",
                onBack = onBack
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Organización", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre de la organización") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it.take(12) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("RUT (opcional)") },
                    placeholder = { Text("12.345.678-9") },
                    isError = !rutValid,
                    supportingText = { if (!rutValid) Text("El RUT no es válido.") },
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = rubroExpanded,
                    onExpandedChange = { rubroExpanded = !rubroExpanded }
                ) {
                    OutlinedTextField(
                        value = rubro,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rubro") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rubroExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = rubroExpanded,
                        onDismissRequest = { rubroExpanded = false }
                    ) {
                        Catalogs.rubros.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    rubro = option
                                    rubroExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = comunaExpanded,
                    onExpandedChange = { comunaExpanded = !comunaExpanded }
                ) {
                    OutlinedTextField(
                        value = comuna,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Comuna") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = comunaExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = comunaExpanded,
                        onDismissRequest = { comunaExpanded = false }
                    ) {
                        Catalogs.comunas.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    comuna = option
                                    comunaExpanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    "Contacto (opcional)",
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = contactName,
                    onValueChange = { contactName = it.take(80) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nombre de contacto") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo de contacto") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it.take(20) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Teléfono de contacto") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { authorized = !authorized }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = authorized, onCheckedChange = { authorized = it })
                    Text(
                        "La persona autorizó ser contactada por PagoFlex.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "Si agregas datos de contacto debes declarar la autorización. La organización recomendada no recibe cobros ni mensajes automáticos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        saving = true
                        viewModel.recommend(
                            nombre = name,
                            rut = rut.ifBlank { null },
                            rubro = rubro,
                            comuna = comuna,
                            contactName = contactName.ifBlank { null },
                            contactEmail = contactEmail.ifBlank { null },
                            contactPhone = contactPhone.ifBlank { null },
                            authorized = authorized
                        ) { result ->
                            saving = false
                            result.onSuccess { created = it }.onFailure { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error.message ?: "No se pudo registrar la recomendación.")
                                }
                            }
                        }
                    },
                    enabled = !saving && name.isNotBlank() && rubro.isNotBlank() && comuna.isNotBlank() && rutValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (saving) "Guardando..." else "Enviar recomendación")
                }
            }
        }
    }
}
