package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import org.json.JSONArray
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtendimentosScreen(viewModel: BarberViewModel) {
    val appointments by viewModel.appointments.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val professionals by viewModel.professionals.collectAsState()
    val services by viewModel.services.collectAsState()
    val products by viewModel.products.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredAppts = remember(appointments, searchQuery) {
        if (searchQuery.isBlank()) {
            appointments
        } else {
            appointments.filter {
                it.clientName.contains(searchQuery, ignoreCase = true) ||
                it.professionalName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Calculated headers for Atendimentos Screen
    val totalRevenue = appointments.sumOf { it.totalValue }
    val totalTickets = if (appointments.isNotEmpty()) totalRevenue / appointments.size else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SUMMARY HEADER ROWS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Atendimentos", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(appointments.size.toString(), color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1.2f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Faturamento Total", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(totalRevenue.toCurrency(), color = BrightTeal, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1.1f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Ticket Médio", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(totalTickets.toCurrency(), color = ActiveGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- SEARCH BAR & ADD FIELD ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por cliente ou profissional...", color = TextMuted) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CoreBlue,
                    unfocusedBorderColor = Color(0xFF334155)
                ),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = CoreBlue),
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = TextLight)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lançar Atendimento", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // --- APPOINTMENTS LIST ---
        if (filteredAppts.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhum atendimento registrado ou correspondente", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAppts) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header: Client Name, Date and Trash
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(app.clientName, color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("Profissional: ${app.professionalName}", color = TextMuted, fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        app.timestamp.toDateString("dd/MM/yyyy HH:mm"),
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = { viewModel.deleteAppointment(app) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remover", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 10.dp))

                            // Services & Products Details block
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Column for items sold
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text("Itens do Atendimento:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Display services
                                    if (app.servicesJson.isNotBlank()) {
                                        val serviceNames = remember(app.servicesJson) {
                                            val list = mutableListOf<String>()
                                            try {
                                                val array = JSONArray(app.servicesJson)
                                                for (k in 0 until array.length()) {
                                                    val sObj = array.getJSONObject(k)
                                                    list.add(sObj.optString("name"))
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                            list
                                        }
                                        serviceNames.forEach { sName ->
                                            Text("• $sName (Serviço)", color = TextLight, fontSize = 12.sp)
                                        }
                                    }

                                    // Display products
                                    if (app.productsJson.isNotBlank()) {
                                        val productNames = remember(app.productsJson) {
                                            val list = mutableListOf<Pair<String, Int>>()
                                            try {
                                                val array = JSONArray(app.productsJson)
                                                for (k in 0 until array.length()) {
                                                    val pObj = array.getJSONObject(k)
                                                    val qty = pObj.optInt("quantity", 1)
                                                    list.add(pObj.optString("name") to qty)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                            list
                                        }
                                        productNames.forEach { (pName, qty) ->
                                            Text("• $pName x$qty (Produto)", color = ActiveGold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                // Column for payment info
                                Column(modifier = Modifier.weight(0.9f), horizontalAlignment = Alignment.End) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF1E293B))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(app.paymentMethod, color = ActiveGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Total:", color = TextMuted, fontSize = 10.sp)
                                    Text(app.totalValue.toCurrency(), color = BrightTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Comissão paga:", color = TextMuted, fontSize = 10.sp)
                                    Text(app.commissionValue.toCurrency(), color = TextLight, fontSize = 11.sp)
                                }
                            }

                            if (app.observations.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Obs: ${app.observations}", color = TextMuted, fontSize = 11.sp, style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                            }
                        }
                    }
                }
            }
        }
    }

    // --- LAUNCH/ADD APPOINTMENT DIALOG ---
    if (showAddDialog) {
        if (clients.isEmpty() || professionals.isEmpty()) {
            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(colors = CardDefaults.cardColors(containerColor = DarkBlueAccent), modifier = Modifier.padding(16.dp)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Aviso Importante", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Você precisa cadastrar pelo menos um Cliente e um Profissional antes de registrar atendimentos.",
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = { showAddDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = CoreBlue)) {
                            Text("Entendido", color = TextLight)
                        }
                    }
                }
            }
        } else {
            var selectedClient by remember { mutableStateOf(clients.first()) }
            var selectedProfessional by remember { mutableStateOf(professionals.first()) }
            var paymentMethod by remember { mutableStateOf("PIX") }
            var remarks by remember { mutableStateOf("") }

            // Services selected set
            val selectedServices = remember { mutableStateListOf<Service>() }
            // Products with quantities
            val selectedProducts = remember { mutableStateMapOf<Int, Int>() } // productId -> quantity

            Dialog(onDismissRequest = { showAddDialog = false }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.9f)
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Registar Atendimento", color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))

                        // Client Select Dropdown simulated
                        Column {
                            Text("Selecione o Cliente *", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                clients.forEach { c ->
                                    val active = selectedClient.id == c.id
                                    FilterChip(
                                        selected = active,
                                        onClick = { selectedClient = c },
                                        label = { Text(c.name, color = if (active) TextLight else TextMuted) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoreBlue)
                                    )
                                }
                            }
                        }

                        // Professional Select Dropdown simulated
                        Column {
                            Text("Selecione o Profissional *", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                professionals.forEach { p ->
                                    val active = selectedProfessional.id == p.id
                                    FilterChip(
                                        selected = active,
                                        onClick = { selectedProfessional = p },
                                        label = { Text(p.name, color = if (active) TextLight else TextMuted) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CoreBlue)
                                    )
                                }
                            }
                        }

                        // Múltiplos Serviços Selection
                        Column {
                            Text("Selecione os Serviços Realizados (Múltiplos)", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            services.filter { it.active }.forEach { s ->
                                val contains = selectedServices.any { it.id == s.id }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (contains) Color(0x201D4ED8) else Color.Transparent)
                                        .clickable {
                                            if (contains) selectedServices.removeAll { it.id == s.id }
                                            else selectedServices.add(s)
                                        }
                                        .padding(vertical = 8.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = contains,
                                            onCheckedChange = {
                                                if (contains) selectedServices.removeAll { it.id == s.id }
                                                else selectedServices.add(s)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(s.name, color = TextLight, fontSize = 13.sp)
                                    }
                                    Text(s.price.toCurrency(), color = ActiveGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Múltiplos Produtos Selection
                        Column {
                            Text("Adicione Produtos Vendidos (Múltiplos)", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            products.filter { it.active }.forEach { p ->
                                val qty = selectedProducts[p.id] ?: 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp, horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(p.name, color = TextLight, fontSize = 13.sp)
                                        Text("Estoque: ${p.stock} un. | ${p.price.toCurrency()}", color = TextMuted, fontSize = 11.sp)
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (qty > 0) {
                                            IconButton(onClick = {
                                                selectedProducts[p.id] = qty - 1
                                                if (selectedProducts[p.id] == 0) selectedProducts.remove(p.id)
                                            }) {
                                                Icon(Icons.Filled.Remove, contentDescription = "Sub", tint = TextMuted)
                                            }
                                            Text(qty.toString(), color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        IconButton(onClick = {
                                            if (qty < p.stock) {
                                                selectedProducts[p.id] = qty + 1
                                            }
                                        }) {
                                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = CoreBlue)
                                        }
                                    }
                                }
                            }
                        }

                        // Forma de Pagamento Select
                        Column {
                            Text("Forma de Pagamento", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Dinheiro", "PIX", "Cartão de débito", "Cartão de crédito", "Outros").forEach { item ->
                                    val active = paymentMethod == item
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) CoreBlue else DeepNavy)
                                            .border(1.dp, if (active) CoreBlue else Color(0xFF334155), RoundedCornerShape(8.dp))
                                            .clickable { paymentMethod = item }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(item, color = if (active) TextLight else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Observations field
                        OutlinedTextField(
                            value = remarks,
                            onValueChange = { remarks = it },
                            label = { Text("Observações do atendimento", color = TextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoreBlue,
                                unfocusedBorderColor = Color(0xFF334155)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Live calculated Total Summary
                        val liveServicesTotal = selectedServices.sumOf { it.price }
                        val liveProductsTotal = selectedProducts.mapNotNull { (prodId, qty) ->
                            products.find { it.id == prodId }?.let { it.price * qty }
                        }.sum()
                        val liveTotalSum = liveServicesTotal + liveProductsTotal

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Calculado:", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(liveTotalSum.toCurrency(), color = BrightTeal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    if (selectedServices.isNotEmpty() || selectedProducts.isNotEmpty()) {
                                        val prodsMapped = selectedProducts.mapNotNull { (pId, qty) ->
                                            products.find { it.id == pId }?.let { it to qty }
                                        }
                                        viewModel.addAppointment(
                                            clientId = selectedClient.id,
                                            clientName = selectedClient.name,
                                            profId = selectedProfessional.id,
                                            profName = selectedProfessional.name,
                                            servicesSelected = selectedServices,
                                            productsSelected = prodsMapped,
                                            paymentMethod = paymentMethod,
                                            timestamp = System.currentTimeMillis(),
                                            obs = remarks
                                        )
                                        showAddDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CoreBlue),
                                enabled = selectedServices.isNotEmpty() || selectedProducts.isNotEmpty()
                            ) {
                                Text("Concluir Lançamento", color = TextLight)
                            }
                        }
                    }
                }
            }
        }
    }
}
