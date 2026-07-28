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
import com.example.data.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceiroScreen(viewModel: BarberViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Expense?>(null) }
    var filterType by remember { mutableStateOf("Todos") } // "Todos", "Fixas", "Variáveis"

    // Filter calculations
    val filteredExpenses = remember(expenses, filterType) {
        when (filterType) {
            "Fixas" -> expenses.filter { it.isFixed }
            "Variáveis" -> expenses.filter { !it.isFixed }
            else -> expenses
        }
    }

    val totalExpensesVal = expenses.sumOf { it.value }
    val totalFixed = expenses.filter { it.isFixed }.sumOf { it.value }
    val totalVariable = expenses.filter { !it.isFixed }.sumOf { it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- EXPENSES SUMMARY ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total Despesas", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(totalExpensesVal.toCurrency(), color = Color(0xFFEF4444), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Despesas Fixas", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(totalFixed.toCurrency(), color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Variáveis", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(totalVariable.toCurrency(), color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- FILTER BUTTONS AND ACTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Todos", "Fixas", "Variáveis").forEach { item ->
                    val active = filterType == item
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (active) CoreBlue else DarkBlueAccent)
                            .clickable { filterType = item }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(item, color = if (active) TextLight else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = TextLight)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Lançar Custo", color = TextLight, fontSize = 13.sp)
            }
        }

        // --- EXPENSES LIST ---
        if (filteredExpenses.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma despesa cadastrada", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredExpenses) { exp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
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
                                    Text(exp.category, color = TextLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (exp.isFixed) Color(0x301D4ED8) else Color(0x30E2E8F0))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (exp.isFixed) "Fixa" else "Variável",
                                            color = if (exp.isFixed) CoreBlue else TextLight,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(exp.description, color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                                Text(exp.timestamp.toDateString("dd/MM/yyyy"), color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(exp.value.toCurrency(), color = Color(0xFFEF4444), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))

                                IconButton(onClick = { showEditDialog = exp }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }

                                IconButton(onClick = { viewModel.deleteExpense(exp) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
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
        var category by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var value by remember { mutableStateOf("") }
        var isFixed by remember { mutableStateOf(true) }

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
                    Text("Lançar Nova Despesa", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoria (Ex: Aluguel, Energia)", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição detalhada", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Valor do custo (R$)", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Classification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tipo de custo", color = TextLight, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Variável", color = if (!isFixed) TextLight else TextMuted, fontSize = 12.sp)
                            Switch(
                                checked = isFixed,
                                onCheckedChange = { isFixed = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextLight,
                                    checkedTrackColor = CoreBlue,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text("Fixa", color = if (isFixed) TextLight else TextMuted, fontSize = 12.sp)
                        }
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
                                val valNum = value.toDoubleOrNull() ?: 0.0
                                if (category.isNotBlank() && valNum > 0) {
                                    viewModel.addExpense(category, description, valNum, System.currentTimeMillis(), isFixed)
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
                        ) {
                            Text("Lançar", color = TextLight)
                        }
                    }
                }
            }
        }
    }

    // --- EDIT DIALOG ---
    if (showEditDialog != null) {
        val expToEdit = showEditDialog!!
        var category by remember { mutableStateOf(expToEdit.category) }
        var description by remember { mutableStateOf(expToEdit.description) }
        var value by remember { mutableStateOf(expToEdit.value.toString()) }
        var isFixed by remember { mutableStateOf(expToEdit.isFixed) }

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
                    Text("Editar Despesa", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Categoria", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição detalhada", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Valor", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Classification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tipo de custo", color = TextLight, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Variável", color = if (!isFixed) TextLight else TextMuted, fontSize = 12.sp)
                            Switch(
                                checked = isFixed,
                                onCheckedChange = { isFixed = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextLight,
                                    checkedTrackColor = CoreBlue,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = Color(0xFF334155)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Text("Fixa", color = if (isFixed) TextLight else TextMuted, fontSize = 12.sp)
                        }
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
                                val valNum = value.toDoubleOrNull() ?: 0.0
                                if (category.isNotBlank() && valNum > 0) {
                                    viewModel.updateExpense(
                                        expToEdit.copy(
                                            category = category,
                                            description = description,
                                            value = valNum,
                                            isFixed = isFixed
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
