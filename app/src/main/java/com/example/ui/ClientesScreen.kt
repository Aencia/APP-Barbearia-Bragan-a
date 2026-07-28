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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Client

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(viewModel: BarberViewModel) {
    val clients by viewModel.clients.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Client?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Filtered client list by name search query
    val filteredClients = remember(clients, searchQuery) {
        if (searchQuery.isBlank()) {
            clients
        } else {
            clients.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Subscription Plans Definition
    val subscriptionPlans = listOf(
        Triple("Nenhum", 0.0, "Sem Assinatura (Avulso)"),
        Triple("Plano Essencial", 70.0, "Plano Essencial - Só Corte (R$ 70,00)"),
        Triple("Plano Executivo", 130.0, "Plano Executivo - Corte + Barba (R$ 130,00)"),
        Triple("Plano VIP Bragança", 180.0, "Plano VIP Bragança (R$ 180,00)")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- CLIENTS HEADER ACTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Gestão de Clientes", color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Gerencie cadastros, planos de assinatura e faturamento", color = TextMuted, fontSize = 13.sp)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = TextLight)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adicionar Cliente", color = TextLight)
            }
        }

        // --- SUBSCRIPTIONS SUMMARY BANNER ---
        val activeSubscriptionsCount = clients.count { it.planActive && it.planName != "Nenhum" }
        val recurringRevenue = clients.filter { it.planActive && it.planName != "Nenhum" }.sumOf { it.planValue }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Clientes", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${clients.size}", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Clubes Ativos", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$activeSubscriptionsCount", color = ActiveGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Recorrência (MRR)", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(recurringRevenue.toCurrency(), color = BrightTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- SEARCH BAR ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar cliente por nome...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CoreBlue,
                unfocusedBorderColor = Color(0xFF334155)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // --- CLIENTS CARD LIST ---
        if (filteredClients.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (searchQuery.isBlank()) "Nenhum cliente cadastrado ainda" else "Nenhum cliente localizado",
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredClients) { client ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Primary Info Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(client.name, color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        if (client.planName != "Nenhum") {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (client.planActive) Color(0x300D9488) else Color(0x3094A3B8))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    "${client.planName} (${if (client.planActive) "Ativo" else "Inativo"})",
                                                    color = if (client.planActive) BrightTeal else TextMuted,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    if (client.phone.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Icon(Icons.Filled.Phone, contentDescription = null, tint = ActiveGold, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(client.phone, color = TextMuted, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { showEditDialog = client }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = TextMuted)
                                    }
                                    IconButton(onClick = { viewModel.deleteClient(client) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 12.dp))

                            val clientAppts = appointments.filter { it.clientId == client.id }
                            val clientBilling = clientAppts.sumOf { it.totalValue }

                            // Historical Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Faturamento total", color = TextMuted, fontSize = 11.sp)
                                    Text(clientBilling.toCurrency(), color = BrightTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Atendimentos", color = TextMuted, fontSize = 11.sp)
                                    Text("${client.totalServices} serviços", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Column {
                                    Text("Último Acesso", color = TextMuted, fontSize = 11.sp)
                                    val dateText = if (client.lastServiceDate > 0) {
                                        client.lastServiceDate.toDateString("dd/MM/yyyy")
                                    } else {
                                        "Sem registro"
                                    }
                                    Text(dateText, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            // Frequency calculation Row
                            Spacer(modifier = Modifier.height(8.dp))
                            val freqText = if (client.totalServices > 1) {
                                val diffInMillis = client.lastServiceDate - client.firstServiceDate
                                val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
                                val avgDays = if (diffInDays > 0) diffInDays / (client.totalServices - 1) else 15L
                                "Retorna a cada $avgDays dias em média"
                            } else {
                                "Cliente novo / Compra única"
                            }
                            Text(
                                freqText,
                                color = if (client.totalServices > 1) BrightTeal else ActiveGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }

    // --- ADD DIALOG ---
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var planName by remember { mutableStateOf("Nenhum") }
        var planValue by remember { mutableStateOf(0.0) }
        var planActive by remember { mutableStateOf(false) }

        var expandedPlanMenu by remember { mutableStateOf(false) }
        val currentPlanIndex = subscriptionPlans.indexOfFirst { it.first == planName }.coerceAtLeast(0)

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Cadastrar Novo Cliente", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Cliente", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("WhatsApp / Celular", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Plan Selection Header
                    Text("Clube de Assinatura", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedPlanMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subscriptionPlans[currentPlanIndex].third,
                                    color = TextLight,
                                    fontSize = 14.sp
                                )
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextMuted)
                            }
                        }

                        DropdownMenu(
                            expanded = expandedPlanMenu,
                            onDismissRequest = { expandedPlanMenu = false },
                            modifier = Modifier
                                .background(DarkBlueAccent)
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        ) {
                            subscriptionPlans.forEachIndexed { index, plan ->
                                DropdownMenuItem(
                                    text = { Text(plan.third, color = TextLight, fontSize = 14.sp) },
                                    onClick = {
                                        planName = plan.first
                                        planValue = plan.second
                                        planActive = plan.first != "Nenhum"
                                        expandedPlanMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (planName != "Nenhum") {
                        // Plan active switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Assinatura Ativa", color = TextLight, fontSize = 13.sp)
                            Switch(
                                checked = planActive,
                                onCheckedChange = { planActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextLight,
                                    checkedTrackColor = CoreBlue,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF334155)
                                )
                            )
                        }

                        // Customizable plan value field
                        var planValStr by remember { mutableStateOf(planValue.toString()) }
                        OutlinedTextField(
                            value = planValStr,
                            onValueChange = { 
                                planValStr = it
                                planValue = it.toDoubleOrNull() ?: planValue
                            },
                            label = { Text("Mensalidade (R$)", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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
                                if (name.isNotBlank()) {
                                    viewModel.addClient(
                                        name = name,
                                        phone = phone,
                                        planName = planName,
                                        planValue = planValue,
                                        planActive = planActive,
                                        planStartDate = if (planActive) System.currentTimeMillis() else 0L
                                    )
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
                        ) {
                            Text("Salvar Cliente", color = TextLight)
                        }
                    }
                }
            }
        }
    }

    // --- EDIT DIALOG ---
    if (showEditDialog != null) {
        val clientToEdit = showEditDialog!!
        var name by remember { mutableStateOf(clientToEdit.name) }
        var phone by remember { mutableStateOf(clientToEdit.phone) }
        var planName by remember { mutableStateOf(clientToEdit.planName) }
        var planValue by remember { mutableStateOf(clientToEdit.planValue) }
        var planActive by remember { mutableStateOf(clientToEdit.planActive) }

        var expandedPlanMenu by remember { mutableStateOf(false) }
        val currentPlanIndex = subscriptionPlans.indexOfFirst { it.first == planName }.coerceAtLeast(0)

        Dialog(onDismissRequest = { showEditDialog = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Editar Cliente", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Cliente", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("WhatsApp / Celular", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Plan Selection Header
                    Text("Clube de Assinatura", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedPlanMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subscriptionPlans[currentPlanIndex].third,
                                    color = TextLight,
                                    fontSize = 14.sp
                                )
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextMuted)
                            }
                        }

                        DropdownMenu(
                            expanded = expandedPlanMenu,
                            onDismissRequest = { expandedPlanMenu = false },
                            modifier = Modifier
                                .background(DarkBlueAccent)
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        ) {
                            subscriptionPlans.forEachIndexed { index, plan ->
                                DropdownMenuItem(
                                    text = { Text(plan.third, color = TextLight, fontSize = 14.sp) },
                                    onClick = {
                                        planName = plan.first
                                        planValue = plan.second
                                        planActive = plan.first != "Nenhum"
                                        expandedPlanMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (planName != "Nenhum") {
                        // Plan active switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Assinatura Ativa", color = TextLight, fontSize = 13.sp)
                            Switch(
                                checked = planActive,
                                onCheckedChange = { planActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextLight,
                                    checkedTrackColor = CoreBlue,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF334155)
                                )
                            )
                        }

                        // Customizable plan value field
                        var planValStr by remember { mutableStateOf(planValue.toString()) }
                        OutlinedTextField(
                            value = planValStr,
                            onValueChange = { 
                                planValStr = it
                                planValue = it.toDoubleOrNull() ?: planValue
                            },
                            label = { Text("Mensalidade (R$)", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

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
                                if (name.isNotBlank()) {
                                    viewModel.updateClient(
                                        clientToEdit.copy(
                                            name = name,
                                            phone = phone,
                                            planName = planName,
                                            planValue = planValue,
                                            planActive = planActive,
                                            planStartDate = if (planActive) {
                                                if (clientToEdit.planStartDate > 0L) clientToEdit.planStartDate else System.currentTimeMillis()
                                            } else 0L
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
