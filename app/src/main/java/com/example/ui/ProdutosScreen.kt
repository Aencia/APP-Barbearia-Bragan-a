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
import com.example.data.Product
import org.json.JSONArray
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdutosScreen(viewModel: BarberViewModel) {
    val products by viewModel.products.collectAsState()
    val appointments by viewModel.appointments.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<Product?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Bebidas", "Cabelo e barba", "Perfumes")

    // Filtered products list
    val filteredProducts = remember(products, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Todos") {
            products
        } else {
            products.filter { it.category == selectedCategoryFilter }
        }
    }

    // Dynamic calculations: Billing per category
    val categoryFaturamento = remember(appointments) {
        val sums = mutableMapOf("Bebidas" to 0.0, "Cabelo e barba" to 0.0, "Perfumes" to 0.0)
        appointments.forEach { app ->
            if (app.productsJson.isNotBlank()) {
                try {
                    val prodArray = JSONArray(app.productsJson)
                    for (i in 0 until prodArray.length()) {
                        val obj = prodArray.getJSONObject(i)
                        val cat = obj.optString("category", "")
                        val price = obj.optDouble("price", 0.0)
                        val qty = obj.optInt("quantity", 1)
                        if (sums.containsKey(cat)) {
                            sums[cat] = sums[cat]!! + (price * qty)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        sums
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- INVENTORY SUMMARY ROWS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Bebidas", "Cabelo e barba", "Perfumes").forEach { cat ->
                val revenue = categoryFaturamento[cat] ?: 0.0
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(cat, color = TextMuted, fontSize = 11.sp, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(revenue.toCurrency(), color = BrightTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- FILTER CHIPS AND BUTTON ACTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scrollable row of category filters
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    val containerColor = if (isSelected) CoreBlue else DarkBlueAccent
                    val contentColor = if (isSelected) TextLight else TextMuted

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(containerColor)
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(cat, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = TextLight)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Novo", color = TextLight, fontSize = 13.sp)
            }
        }

        // --- LIST OF PRODUCTS ---
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum produto cadastrado nesta categoria", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { prod ->
                    // Margem de lucro calculation: Preço - Custo
                    val profitAmt = (prod.price - prod.cost)
                    val marginPct = if (prod.price > 0) (profitAmt / prod.price) * 100 else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (prod.active) DarkBlueAccent else Color(0xFF1E242E)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Title & Actions Group
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            prod.name,
                                            color = if (prod.active) TextLight else TextMuted,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!prod.active) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("Inativo", color = Color(0xFFEF4444), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(prod.category, color = ActiveGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Switch active state
                                    Switch(
                                        checked = prod.active,
                                        onCheckedChange = { isChecked ->
                                            viewModel.updateProduct(prod.copy(active = isChecked))
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = TextLight,
                                            checkedTrackColor = CoreBlue,
                                            uncheckedThumbColor = TextMuted,
                                            uncheckedTrackColor = Color(0xFF334155)
                                        )
                                    )

                                    IconButton(onClick = { showEditDialog = prod }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = TextMuted)
                                    }
                                    IconButton(onClick = { viewModel.deleteProduct(prod) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 12.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Estoque", color = TextMuted, fontSize = 11.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${prod.stock} un.", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Stock alerts
                                        when {
                                            prod.stock == 0 -> {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFFF3333).copy(alpha = 0.2f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Zerar", color = Color(0xFFFF3333), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            prod.stock <= 5 -> {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFF1C40F).copy(alpha = 0.2f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Baixo", color = Color(0xFFF1C40F), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Column {
                                    Text("Custo", color = TextMuted, fontSize = 11.sp)
                                    Text(prod.cost.toCurrency(), color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                Column {
                                    Text("Preço", color = TextMuted, fontSize = 11.sp)
                                    Text(prod.price.toCurrency(), color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                Column {
                                    Text("Margem de Lucro", color = TextMuted, fontSize = 11.sp)
                                    Text(
                                        "${profitAmt.toCurrency()} (${String.format(Locale.getDefault(), "%.1f%%", marginPct)})",
                                        color = BrightTeal,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
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
        var category by remember { mutableStateOf("Bebidas") }
        var stock by remember { mutableStateOf("10") }
        var cost by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var commission by remember { mutableStateOf("10") }

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
                    Text("Cadastrar Novo Produto", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Produto", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Simple category selector dropdown simulated via row options
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Categoria", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Bebidas", "Cabelo e barba", "Perfumes").forEach { cat ->
                                val active = category == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) CoreBlue else DeepNavy)
                                        .border(1.dp, if (active) CoreBlue else Color(0xFF334155), RoundedCornerShape(8.dp))
                                        .clickable { category = cat }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, color = if (active) TextLight else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Estoque", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = commission,
                            onValueChange = { commission = it },
                            label = { Text("Comissão (%)", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = cost,
                            onValueChange = { cost = it },
                            label = { Text("Custo (R$)", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Preço Venda", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
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
                                val sVal = stock.toIntOrNull() ?: 0
                                val cVal = cost.toDoubleOrNull() ?: 0.0
                                val pVal = price.toDoubleOrNull() ?: 0.0
                                val comVal = commission.toDoubleOrNull() ?: 10.0
                                if (name.isNotBlank() && pVal > 0) {
                                    viewModel.addProduct(name, category, sVal, cVal, pVal, comVal)
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
        val prodToEdit = showEditDialog!!
        var name by remember { mutableStateOf(prodToEdit.name) }
        var category by remember { mutableStateOf(prodToEdit.category) }
        var stock by remember { mutableStateOf(prodToEdit.stock.toString()) }
        var cost by remember { mutableStateOf(prodToEdit.cost.toString()) }
        var price by remember { mutableStateOf(prodToEdit.price.toString()) }
        var commission by remember { mutableStateOf(prodToEdit.commissionPercentage.toString()) }

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
                    Text("Editar Produto", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Produto", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Category selection
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Categoria", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Bebidas", "Cabelo e barba", "Perfumes").forEach { cat ->
                                val active = category == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) CoreBlue else DeepNavy)
                                        .border(1.dp, if (active) CoreBlue else Color(0xFF334155), RoundedCornerShape(8.dp))
                                        .clickable { category = cat }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cat, color = if (active) TextLight else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Estoque", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = commission,
                            onValueChange = { commission = it },
                            label = { Text("Comissão (%)", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = cost,
                            onValueChange = { cost = it },
                            label = { Text("Custo (R$)", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Preço Venda", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
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
                                val sVal = stock.toIntOrNull() ?: 0
                                val cVal = cost.toDoubleOrNull() ?: 0.0
                                val pVal = price.toDoubleOrNull() ?: 0.0
                                val comVal = commission.toDoubleOrNull() ?: 10.0
                                if (name.isNotBlank() && pVal > 0) {
                                    viewModel.updateProduct(
                                        prodToEdit.copy(
                                            name = name,
                                            category = category,
                                            stock = sVal,
                                            cost = cVal,
                                            price = pVal,
                                            commissionPercentage = comVal
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
