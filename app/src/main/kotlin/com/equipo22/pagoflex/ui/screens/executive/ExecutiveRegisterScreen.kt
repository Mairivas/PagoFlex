package com.equipo22.pagoflex.ui.screens.executive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo22.pagoflex.data.Catalogs
import com.equipo22.pagoflex.data.CommitmentEntity
import com.equipo22.pagoflex.ui.components.PagoFlexTopBar
import com.equipo22.pagoflex.utils.addDays
import com.equipo22.pagoflex.utils.cleanAmount
import com.equipo22.pagoflex.utils.formatCurrency
import com.equipo22.pagoflex.utils.formatDate
import com.equipo22.pagoflex.utils.isValidRut
import com.equipo22.pagoflex.utils.parseDate
import com.equipo22.pagoflex.viewmodel.ExecutiveViewModel
import java.util.Date
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutiveRegisterScreen(
    viewModel: ExecutiveViewModel,
    onBack: () -> Unit,
    onCreated: () -> Unit
) {
    val company by viewModel.company.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var rut by remember { mutableStateOf("") }
    var names by remember { mutableStateOf("") }
    var surnames by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var commune by remember { mutableStateOf("") }
    var communeExpanded by remember { mutableStateOf(false) }
    var concept by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(formatDate(addDays(Date(), 30))) }
    var installmentText by remember { mutableStateOf("") }
    var totalText by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var created by remember { mutableStateOf<CommitmentEntity?>(null) }
    val amount = amountText.toLongOrNull() ?: 0L
    var personMessage by remember { mutableStateOf<String?>(null) }

    if (created != null) {
        AlertDialog(
            onDismissRequest = onCreated,
            title = { Text("Compromiso registrado") },
            text = {
                Text(
                    "Se creó el compromiso ${created?.folio} por ${formatCurrency(created?.monto ?: 0)}. La persona quedó habilitada para pagarlo desde la app."
                )
            },
            confirmButton = {
                TextButton(onClick = onCreated) {
                    Text("Volver a la cartera")
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
                title = "Registrar compromiso",
                subtitle = company?.nombreFantasia ?: "Mi empresa",
                onBack = onBack
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Persona deudora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rut,
                        onValueChange = { rut = it.take(12) },
                        modifier = Modifier.weight(1f),
                        label = { Text("RUT") },
                        placeholder = { Text("12.345.678-9") },
                        isError = rut.isNotBlank() && !isValidRut(rut),
                        singleLine = true
                    )
                    OutlinedButton(
                        onClick = {
                            viewModel.findPerson(rut) { person ->
                                if (person == null) {
                                    personMessage = "No existe una persona con ese RUT. Completa sus datos para crearla."
                                } else {
                                    names = person.nombres
                                    surnames = person.apellidos
                                    email = person.correo
                                    phone = person.telefono
                                    commune = person.comuna
                                    personMessage = "Datos cargados desde el registro existente."
                                }
                            }
                        },
                        enabled = isValidRut(rut),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Buscar")
                    }
                }
                personMessage?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = names,
                        onValueChange = { names = it.take(60) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nombres") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = surnames,
                        onValueChange = { surnames = it.take(60) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Apellidos") },
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it.take(120) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.take(25) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = communeExpanded,
                    onExpandedChange = { communeExpanded = !communeExpanded }
                ) {
                    OutlinedTextField(
                        value = commune,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Comuna") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = communeExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = communeExpanded,
                        onDismissRequest = { communeExpanded = false }
                    ) {
                        Catalogs.comunas.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    commune = option
                                    communeExpanded = false
                                }
                            )
                        }
                    }
                }
                Text(
                    "Si el RUT no existe, se creará una persona con los datos ingresados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Compromiso", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = concept,
                    onValueChange = { concept = it.take(80) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Concepto") },
                    placeholder = { Text("Cuota de arriendo noviembre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = cleanAmount(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monto") },
                    placeholder = { Text("150000") },
                    supportingText = { Text(formatCurrency(amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it.take(10) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Fecha de vencimiento") },
                    placeholder = { Text("DD-MM-AAAA") },
                    isError = parseDate(dueDate) == null,
                    supportingText = {
                        if (parseDate(dueDate) == null) {
                            Text("Usa el formato DD-MM-AAAA.")
                        } else {
                            Text("La fecha no puede ser anterior a hoy.")
                        }
                    },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { dueDate = formatDate(addDays(Date(), 7)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("En 7 días")
                    }
                    OutlinedButton(
                        onClick = { dueDate = formatDate(addDays(Date(), 30)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("En 30 días")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = installmentText,
                        onValueChange = { installmentText = cleanAmount(it).take(3) },
                        modifier = Modifier.weight(1f),
                        label = { Text("N° de cuota") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = totalText,
                        onValueChange = { totalText = cleanAmount(it).take(3) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Total de cuotas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                Text(
                    "Las cuotas son opcionales. Si las informas, completa ambos campos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        val currentCompany = company ?: return@Button
                        saving = true
                        viewModel.registerCommitment(
                            companyId = currentCompany.id,
                            rut = rut,
                            names = names,
                            surnames = surnames,
                            email = email,
                            phone = phone,
                            commune = commune,
                            concept = concept,
                            amount = amount,
                            dueDate = dueDate,
                            installment = installmentText.toIntOrNull(),
                            totalInstallments = totalText.toIntOrNull()
                        ) { result ->
                            saving = false
                            result.onSuccess { created = it }.onFailure { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error.message ?: "No se pudo registrar el compromiso.")
                                }
                            }
                        }
                    },
                    enabled = !saving && isValidRut(rut) && concept.isNotBlank() && amount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(if (saving) "Registrando..." else "Registrar compromiso")
                }
            }
        }
    }
}
