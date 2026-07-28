package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Service

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicosScreen(viewModel: BarberViewModel) {
    val services by viewModel.services.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Service?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SCREEN HEADER ACTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tabela de Serviços", color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Gerencie os serviços oferecidos e comissões", color = TextMuted, fontSize = 13.sp)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = TextLight)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Novo Serviço", color = TextLight)
            }
        }

        // --- SERVICES LIST ---
        if (services.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum serviço cadastrado", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(services) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (service.active) DarkBlueAccent else Color(0xFF1E242E)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        service.name,
                                        color = if (service.active) TextLight else TextMuted,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!service.active) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Inativo", fontSize = 10.sp) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = Color(0x30EF4444),
                                                labelColor = Color(0xFFEF4444)
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${service.durationMinutes} min", color = TextMuted, fontSize = 12.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Percent, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${service.commissionPercentage}% comissão", color = TextMuted, fontSize = 12.sp)
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    service.price.toCurrency(),
                                    color = if (service.active) ActiveGold else TextMuted,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Active state Switch
                                Switch(
                                    checked = service.active,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateService(service.copy(active = isChecked))
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TextLight,
                                        checkedTrackColor = CoreBlue,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = Color(0xFF334155)
                                    )
                                )

                                IconButton(onClick = { showEditDialog = service }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- ADD DIALOG ---
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var duration by remember { mutableStateOf("30") }
        var commission by remember { mutableStateOf("45") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Cadastrar Novo Serviço", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Serviço", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Preço de Venda (R$)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duração (Minutos)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = commission,
                        onValueChange = { commission = it },
                        label = { Text("Comissão do Profissional (%)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val pVal = price.toDoubleOrNull() ?: 0.0
                                val dVal = duration.toIntOrNull() ?: 30
                                val cVal = commission.toDoubleOrNull() ?: 45.0
                                if (name.isNotBlank() && pVal > 0) {
                                    viewModel.addService(name, pVal, dVal, cVal)
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
                        ) {
                            Text("Salvar Serviço", color = TextLight)
                        }
                    }
                }
            }
        }
    }

    // --- EDIT DIALOG ---
    if (showEditDialog != null) {
        val servToEdit = showEditDialog!!
        var name by remember { mutableStateOf(servToEdit.name) }
        var price by remember { mutableStateOf(servToEdit.price.toString()) }
        var duration by remember { mutableStateOf(servToEdit.durationMinutes.toString()) }
        var commission by remember { mutableStateOf(servToEdit.commissionPercentage.toString()) }

        Dialog(onDismissRequest = { showEditDialog = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Editar Serviço", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Serviço", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Preço de Venda (R$)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duração (Minutos)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = commission,
                        onValueChange = { commission = it },
                        label = { Text("Comissão do Profissional (%)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showEditDialog = null }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val pVal = price.toDoubleOrNull() ?: 0.0
                                val dVal = duration.toIntOrNull() ?: 30
                                val cVal = commission.toDoubleOrNull() ?: 45.0
                                if (name.isNotBlank() && pVal > 0) {
                                    viewModel.updateService(
                                        servToEdit.copy(
                                            name = name,
                                            price = pVal,
                                            durationMinutes = dVal,
                                            commissionPercentage = cVal
                                        )
                                    )
                                    showEditDialog = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
                        ) {
                            Text("Salvar Alterações", color = TextLight)
                        }
                    }
                }
            }
        }
    }
}
