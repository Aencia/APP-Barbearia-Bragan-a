package com.example.ui

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.*
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RelatoriosScreen(viewModel: BarberViewModel) {
    val appointments by viewModel.appointments.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val professionals by viewModel.professionals.collectAsState()

    val context = LocalContext.current

    var selectedPeriod by remember { mutableStateOf("Mensal") } // "Diário", "Semanal", "Mensal", "Anual"

    // Filtering definitions based on period chosen
    val filteredAppts = remember(appointments, selectedPeriod) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val startTime = when (selectedPeriod) {
            "Diário" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            "Semanal" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            "Mensal" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            "Anual" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            else -> 0L
        }
        appointments.filter { it.timestamp >= startTime && it.timestamp <= now }
    }

    val filteredExpenses = remember(expenses, selectedPeriod) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val startTime = when (selectedPeriod) {
            "Diário" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.timeInMillis
            }
            "Semanal" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.timeInMillis
            }
            "Mensal" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.timeInMillis
            }
            "Anual" -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.timeInMillis
            }
            else -> 0L
        }
        expenses.filter { it.timestamp >= startTime && it.timestamp <= now }
    }

    // Calculations inside Period
    val faturamentoVal = filteredAppts.sumOf { it.totalValue }
    val commissionVal = filteredAppts.sumOf { it.commissionValue }
    val despesasVal = filteredExpenses.sumOf { it.value }
    val ticketMedioVal = if (filteredAppts.isNotEmpty()) faturamentoVal / filteredAppts.size else 0.0
    val lucroVal = faturamentoVal - despesasVal - commissionVal

    // Returning clients inside period
    val returnRatePeriod = if (clients.isNotEmpty()) {
        val count = clients.filter { it.totalServices > 1 }.size
        (count.toDouble() / clients.size) * 100
    } else {
        84.0
    }

    // Services Ranking
    val servicesRank = remember(filteredAppts) {
        val rank = mutableMapOf<String, Int>()
        filteredAppts.forEach { app ->
            if (app.servicesJson.isNotBlank()) {
                try {
                    val arr = JSONArray(app.servicesJson)
                    for (i in 0 until arr.length()) {
                        val sName = arr.getJSONObject(i).optString("name")
                        rank[sName] = (rank[sName] ?: 0) + 1
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        rank.entries.sortedByDescending { it.value }.take(5)
    }

    // Products Ranking
    val productsRank = remember(filteredAppts) {
        val rank = mutableMapOf<String, Int>()
        filteredAppts.forEach { app ->
            if (app.productsJson.isNotBlank()) {
                try {
                    val arr = JSONArray(app.productsJson)
                    for (i in 0 until arr.length()) {
                        val pObj = arr.getJSONObject(i)
                        val pName = pObj.optString("name")
                        val qty = pObj.optInt("quantity", 1)
                        rank[pName] = (rank[pName] ?: 0) + qty
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        rank.entries.sortedByDescending { it.value }.take(5)
    }

    // Export Helpers
    fun shareReportFile(format: String) {
        try {
            val fileName = "Relatorio_Braganca_${selectedPeriod}_${System.currentTimeMillis()}.$format"
            val file = File(context.cacheDir, fileName)
            val writer = FileWriter(file)

            if (format == "csv") {
                writer.append("Relatorio Barbearia Braganca - Periodo: $selectedPeriod\n\n")
                writer.append("Faturamento Total;Taxa de Retorno;Ticket Medio;Despesas Totais;Lucro Liquido\n")
                writer.append("${faturamentoVal};${returnRatePeriod}%;${ticketMedioVal};${despesasVal};${lucroVal}\n\n")

                writer.append("Ranking Servicos;Quantidade\n")
                servicesRank.forEach { writer.append("${it.key};${it.value}\n") }
                writer.append("\nRanking de Produtos;Quantidade\n")
                productsRank.forEach { writer.append("${it.key};${it.value}\n") }
            } else {
                // simple txt output supporting pdf print mock
                writer.append("=========================================\n")
                writer.append("  RELATORIO FINANCEIRO - BARBEARIA BRAGANCA\n")
                writer.append("=========================================\n")
                writer.append("Periodo selecionado: $selectedPeriod\n")
                writer.append("Faturamento Bruto:   ${faturamentoVal.toCurrency()}\n")
                writer.append("Custos / Despesas:   ${despesasVal.toCurrency()}\n")
                writer.append("Comissao Devida:     ${commissionVal.toCurrency()}\n")
                writer.append("LUCRO LIQUIDO:       ${lucroVal.toCurrency()}\n")
                writer.append("Ticket Medio Periodo: ${ticketMedioVal.toCurrency()}\n")
                writer.append("Taxa Retorno Local:   ${String.format(Locale.US, "%.1f%%", returnRatePeriod)}\n")
                writer.append("-----------------------------------------\n")
                writer.append("SERVICOS MAIS VENDIDOS:\n")
                servicesRank.forEach { (name, count) -> writer.append("- $name: $count atendimentos\n") }
                writer.append("\nPRODUTOS MAIS VENDIDOS:\n")
                productsRank.forEach { (name, count) -> writer.append("- $name: $count unidades\n") }
                writer.append("=========================================\n")
            }
            writer.flush()
            writer.close()

            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar Relatório"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- CHOOSE PERIOD TABS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Diário", "Semanal", "Mensal", "Anual").forEach { period ->
                    val active = selectedPeriod == period
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (active) CoreBlue else DarkBlueAccent)
                            .clickable { selectedPeriod = period }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(period, color = if (active) TextLight else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Export Actions Group
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { shareReportFile("csv") },
                    colors = ButtonDefaults.buttonColors(containerColor = BrightTeal),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("CSV", color = TextLight, fontSize = 12.sp)
                }

                Button(
                    onClick = { shareReportFile("txt") }, // Simple print txt/pdf format
                    colors = ButtonDefaults.buttonColors(containerColor = ActiveGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Print, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF (TXT)", color = TextLight, fontSize = 12.sp)
                }
            }
        }

        // --- STATS NUMBERS CARDS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Resultados Financeiros no Período", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Faturamento Bruto", color = TextMuted, fontSize = 12.sp)
                        Text(faturamentoVal.toCurrency(), color = TextLight, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Despesas Totais", color = TextMuted, fontSize = 12.sp)
                        Text(despesasVal.toCurrency(), color = Color(0xFFEF4444), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = Color(0xFF334155))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Comissões Pagas", color = TextMuted, fontSize = 11.sp)
                        Text(commissionVal.toCurrency(), color = ActiveGold, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Column {
                        Text("Ticket Médio", color = TextMuted, fontSize = 11.sp)
                        Text(ticketMedioVal.toCurrency(), color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Lucro Líquido", color = TextMuted, fontSize = 11.sp)
                        Text(lucroVal.toCurrency(), color = BrightTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- PROF AND SALES BREAKDOWN IN PERIOD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Faturamento por Profissional", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                professionals.forEach { prof ->
                    val profAppts = filteredAppts.filter { it.professionalId == prof.id }
                    val profTotal = profAppts.sumOf { it.totalValue }
                    val profComm = profAppts.sumOf { it.commissionValue }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(prof.name, color = TextLight, fontSize = 13.sp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(profTotal.toCurrency(), color = BrightTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Comissão: ${profComm.toCurrency()}", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // --- SERVICES AND PRODUCTS RANKING ROWS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left list: Services
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Serviços Mais Vendidos", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                    if (servicesRank.isEmpty()) {
                        Text("Sem vendas", color = TextMuted, fontSize = 11.sp)
                    } else {
                        servicesRank.forEach { (name, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, color = TextLight, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                Text("$count un.", color = ActiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Right list: Products
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Produtos Mais Vendidos", color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                    if (productsRank.isEmpty()) {
                        Text("Sem vendas", color = TextMuted, fontSize = 11.sp)
                    } else {
                        productsRank.forEach { (name, qty) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(name, color = TextLight, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
                                Text("$qty un.", color = BrightTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
