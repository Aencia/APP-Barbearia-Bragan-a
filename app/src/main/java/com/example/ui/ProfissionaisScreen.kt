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
import com.example.data.Professional
import org.json.JSONArray
import java.util.*

@Composable
fun ProfissionaisScreen(viewModel: BarberViewModel) {
    val professionals by viewModel.professionals.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Professional?>(null) }

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
                Text("Profissionais e Desempenho", color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Acompanhe faturamento individual e metas", color = TextMuted, fontSize = 13.sp)
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = TextLight)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Adicionar", color = TextLight)
            }
        }

        // --- PROF LIST WITH CALCULATED METRICS ---
        if (professionals.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum profissional cadastrado", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(professionals) { prof ->
                    // Calculate individual metrics based on Appointments
                    val profAppts = appointments.filter { it.professionalId == prof.id }
                    val billingTotal = profAppts.sumOf { it.totalValue }
                    val ticketAverage = if (profAppts.isNotEmpty()) billingTotal / profAppts.size else 0.0
                    val commissionTotal = profAppts.sumOf { it.commissionValue }

                    // Sum of product values sold and quantities
                    var productsCount = 0
                    var productsValueTotal = 0.0

                    profAppts.forEach { app ->
                        if (app.productsJson.isNotBlank()) {
                            try {
                                val prodArray = JSONArray(app.productsJson)
                                for (j in 0 until prodArray.length()) {
                                    val jObj = prodArray.getJSONObject(j)
                                    val qty = jObj.optInt("quantity", 1)
                                    val price = jObj.optDouble("price", 0.0)
                                    productsCount += qty
                                    productsValueTotal += (price * qty)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    // Percent of product sales goal reached (goal is usually R$ 500,00)
                    val goalReachedPct = if (prof.productSalesGoal > 0.0) {
                        (productsValueTotal / prof.productSalesGoal) * 100.0
                    } else {
                        100.0
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(prof.name, color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text(prof.role, color = ActiveGold, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                }
                                Row {
                                    IconButton(onClick = { showEditDialog = prof }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = TextMuted)
                                    }
                                    IconButton(onClick = { viewModel.deleteProfessional(prof) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 14.dp))

                            // Grid of indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Faturamento", color = TextMuted, fontSize = 11.sp)
                                    Text(billingTotal.toCurrency(), color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ticket Médio", color = TextMuted, fontSize = 11.sp)
                                    Text(ticketAverage.toCurrency(), color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Comissão Acumulada", color = TextMuted, fontSize = 11.sp)
                                    Text(commissionTotal.toCurrency(), color = BrightTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(modifier = Modifier.weight(0.8f)) {
                                    Text("Itens Vendidos", color = TextMuted, fontSize = 11.sp)
                                    Text("$productsCount unid.", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Product Sales Goal Tracker
                            val pctClamped = (goalReachedPct / 100).toFloat().coerceIn(0f, 1f)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Meta de Vendas de Produtos (Mensal)",
                                        color = TextLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        "${productsValueTotal.toCurrency()} / ${prof.productSalesGoal.toCurrency()} (${String.format(Locale.getDefault(), "%.1f%%", goalReachedPct)})",
                                        color = if (goalReachedPct >= 100.0) BrightTeal else TextLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF334155))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(pctClamped)
                                            .fillMaxHeight()
                                            .background(if (goalReachedPct >= 100.0) BrightTeal else ActiveGold)
                                    )
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
        var role by remember { mutableStateOf("Barbeiro") }
        var commission by remember { mutableStateOf("45") }
        var goal by remember { mutableStateOf("500") }

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
                    Text("Cadastrar Novo Profissional", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Cargo (Ex: Barbeiro, Estilista)", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = commission,
                        onValueChange = { commission = it },
                        label = { Text("Comissão Serviço (%)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Meta de Vendas de Produtos (R$)", color = TextMuted) },
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
                                val comValue = commission.toDoubleOrNull() ?: 45.0
                                val goalValue = goal.toDoubleOrNull() ?: 500.0
                                if (name.isNotBlank() && role.isNotBlank()) {
                                    viewModel.addProfessional(name, role, comValue, goalValue)
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
                        ) {
                            Text("Salvar", color = TextLight)
                        }
                    }
                }
            }
        }
    }

    // --- EDIT DIALOG ---
    if (showEditDialog != null) {
        val profToEdit = showEditDialog!!
        var name by remember { mutableStateOf(profToEdit.name) }
        var role by remember { mutableStateOf(profToEdit.role) }
        var commission by remember { mutableStateOf(profToEdit.serviceCommissionPercentage.toString()) }
        var goal by remember { mutableStateOf(profToEdit.productSalesGoal.toString()) }

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
                    Text("Editar Profissional", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome Completo", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Cargo (Ex: Barbeiro, Estilista)", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = commission,
                        onValueChange = { commission = it },
                        label = { Text("Comissão Serviço (%)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Meta de Vendas de Produtos (R$)", color = TextMuted) },
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
                                val comValue = commission.toDoubleOrNull() ?: 45.0
                                val goalValue = goal.toDoubleOrNull() ?: 500.0
                                if (name.isNotBlank() && role.isNotBlank()) {
                                    viewModel.updateProfessional(
                                        profToEdit.copy(
                                            name = name,
                                            role = role,
                                            serviceCommissionPercentage = comValue,
                                            productSalesGoal = goalValue
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
