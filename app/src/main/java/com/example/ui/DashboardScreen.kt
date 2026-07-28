package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Moving
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import org.json.JSONArray
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(viewModel: BarberViewModel) {
    val appointments by viewModel.appointments.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val clients by viewModel.clients.collectAsState()
    val currentGoal by viewModel.currentGoal.collectAsState()
    val professionals by viewModel.professionals.collectAsState()

    val scrollState = rememberScrollState()

    // Date filters setup (Current month)
    val calendar = Calendar.getInstance()
    val todayStart = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    val todayEnd = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis

    // Month start / end
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val monthStart = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis

    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    val monthEnd = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
    }.timeInMillis

    // For current week definitions
    calendar.time = Date()
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    val weekStart = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.timeInMillis
    val weekEnd = weekStart + 7 * 24 * 60 * 60 * 1000L

    // Metrics Calculations
    val faturamentoDia = appointments.filter { it.timestamp in todayStart..todayEnd }.sumOf { it.totalValue }
    val faturamentoSemana = appointments.filter { it.timestamp in weekStart..weekEnd }.sumOf { it.totalValue }
    val faturamentoMes = appointments.filter { it.timestamp in monthStart..monthEnd }.sumOf { it.totalValue }

    val ticketMedio = if (appointments.isNotEmpty()) {
        appointments.sumOf { it.totalValue } / appointments.size
    } else {
        71.19 // Default reference if empty
    }

    val atendimentosMesCount = appointments.filter { it.timestamp in monthStart..monthEnd }.size

    // Return rate: Proportion of clients with totalServices > 1
    val taxaRetorno = if (clients.isNotEmpty()) {
        val returningClients = clients.filter { it.totalServices > 1 }.size
        (returningClients.toDouble() / clients.size.toDouble()) * 100.0
    } else {
        84.0 // Reference
    }

    val totalDespesasMes = expenses.filter { it.timestamp in monthStart..monthEnd }.sumOf { it.value }
    val totalComissaoMes = appointments.filter { it.timestamp in monthStart..monthEnd }.sumOf { it.commissionValue }
    val lucroLiquido = faturamentoMes - totalDespesasMes - totalComissaoMes

    // Forecast: based on daily average
    val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val totalDaysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
    val faturamentoPrevisao = if (dayOfMonth > 0) {
        (faturamentoMes / dayOfMonth) * totalDaysInMonth
    } else {
        faturamentoMes
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- WELCOME HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Visão Geral do Negócio", color = TextLight, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Indicadores atualizados em tempo real", color = TextMuted, fontSize = 13.sp)
            }
            // Date Tag
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Date", tint = ActiveGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(Date()).replaceFirstChar { it.uppercase() },
                        color = TextLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- INDICATORS ROW GRID ---
        val indicators = listOf(
            IndicatorData("Faturamento Dia", faturamentoDia.toCurrency(), Icons.Filled.Payments, BrightTeal),
            IndicatorData("Faturamento Semana", faturamentoSemana.toCurrency(), Icons.Filled.Savings, BrightTeal),
            IndicatorData("Faturamento Mês", faturamentoMes.toCurrency(), Icons.Filled.MonetizationOn, CoreBlue),
            IndicatorData("Ticket Médio Geral", ticketMedio.toCurrency(), Icons.Filled.LocalAtm, ActiveGold),
            IndicatorData("Atendimentos Mês", atendimentosMesCount.toString(), Icons.Filled.ContentCut, ActiveGold),
            IndicatorData("Taxa de Retorno", String.format(Locale.US, "%.1f%%", taxaRetorno), Icons.Filled.Loop, BrightTeal),
            IndicatorData("Despesas Mês", totalDespesasMes.toCurrency(), Icons.Filled.MoneyOff, Color(0xFFEF4444)),
            IndicatorData("Comissão Devida", totalComissaoMes.toCurrency(), Icons.Filled.AccountBalanceWallet, Color(0xFFF59E0B)),
            IndicatorData("Lucro Líquido", lucroLiquido.toCurrency(), Icons.Filled.TrendingUp, BrightTeal),
            IndicatorData("Previsão Mensal", faturamentoPrevisao.toCurrency(), Icons.Outlined.Moving, CoreBlue)
        )

        // Make it flexible with grid
        Column {
            Text("Principais Indicadores", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 4,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val boxWidth = if (Locale.getDefault().language == "pt") 165.dp else 165.dp
                indicators.forEach { ind ->
                    Card(
                        modifier = Modifier
                            .widthIn(min = 160.dp)
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ind.title, color = TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Icon(ind.icon, contentDescription = ind.title, tint = ind.iconColor, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(ind.value, color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- BIILING GOAL PROGRESSES ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Progresso das Metas de Faturamento",
                    color = TextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                val goal = currentGoal ?: BillingGoal("Current", 18250.00, 20000.00, 25000.00)

                // 1. Minimum Goal
                GoalProgressBar(
                    title = "Meta Mínima",
                    score = faturamentoMes,
                    target = goal.minGoal,
                    accentColor = BrightTeal
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Medium Goal
                GoalProgressBar(
                    title = "Meta Média (Desejada)",
                    score = faturamentoMes,
                    target = goal.medGoal,
                    accentColor = CoreBlue
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. High Goal
                GoalProgressBar(
                    title = "Meta Alta (Excelente)",
                    score = faturamentoMes,
                    target = goal.highGoal,
                    accentColor = ActiveGold
                )
            }
        }

        // --- CHARTS SECTION (TWO COLUMN ON EXPANDED, FLOWING ON COMPACT) ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Gráficos de Desempenho", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            // Dynamic layout: side by side if possible or stacked
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Chart 1: Revenue Evolution Split by Week
                Card(
                    modifier = Modifier.weight(1f).widthIn(min = 340.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Evolução do Faturamento (Semanal)", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Distribuição do faturamento no mês", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp))

                        // Compute billing per week
                        val weekMetrics = DoubleArray(4) { 0.0 }
                        appointments.filter { it.timestamp in monthStart..monthEnd }.forEach { app ->
                            val cal = Calendar.getInstance().apply { timeInMillis = app.timestamp }
                            val dom = cal.get(Calendar.DAY_OF_MONTH)
                            when {
                                dom <= 7 -> weekMetrics[0] += app.totalValue
                                dom <= 14 -> weekMetrics[1] += app.totalValue
                                dom <= 21 -> weekMetrics[2] += app.totalValue
                                else -> weekMetrics[3] += app.totalValue
                            }
                        }

                        // Render custom bars
                        val maxWeekVal = weekMetrics.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            for (index in 0..3) {
                                val valWeek = weekMetrics[index]
                                val pct = (valWeek / maxWeekVal).toFloat()

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (valWeek > 0) String.format(Locale.US, "R$ %.0f", valWeek) else "R$ 0",
                                        color = TextLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .fillMaxHeight(0.8f * pct)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(CoreBlue, Color(0x701D4ED8))
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Semana ${index + 1}", color = TextMuted, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Chart 2: Expenses Distribution
                Card(
                    modifier = Modifier.weight(1f).widthIn(min = 340.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Distribuição de Despesas Fixas", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Maiores custos registrados no sistema", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

                        // Take up to 5 largest expenses
                        val largestExpenses = expenses.filter { it.timestamp in monthStart..monthEnd }
                            .sortedByDescending { it.value }
                            .take(5)

                        if (largestExpenses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhuma despesa mensal registrada", color = TextMuted, fontSize = 13.sp)
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val maxExp = largestExpenses.first().value.coerceAtLeast(1.0)
                                largestExpenses.forEach { exp ->
                                    val pct = (exp.value / maxExp).toFloat()
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(exp.category, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Text(exp.value.toCurrency(), color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF334155))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(pct)
                                                    .fillMaxHeight()
                                                    .background(Color(0xFFEF4444))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Chart 3: Faturamento por Profissional
                Card(
                    modifier = Modifier.weight(1f).widthIn(min = 340.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Faturamento por Profissional", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Soma de serviços em atendimentos realizados", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp))

                        val profBillingMap = mutableMapOf<String, Double>()
                        // Seed professionals names for safety
                        professionals.forEach { p -> profBillingMap[p.name] = 0.0 }
                        appointments.filter { it.timestamp in monthStart..monthEnd }.forEach { app ->
                            profBillingMap[app.professionalName] = (profBillingMap[app.professionalName] ?: 0.0) + app.totalValue
                        }

                        val maxProfBill = profBillingMap.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            profBillingMap.forEach { (name, bill) ->
                                val pct = (bill / maxProfBill).toFloat()
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(name, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(bill.toCurrency(), color = ActiveGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color(0xFF334155))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(pct)
                                                .fillMaxHeight()
                                                .background(ActiveGold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Chart 4: Product Sales Categories
                Card(
                    modifier = Modifier.weight(1f).widthIn(min = 340.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Vendas de Produtos por Categoria", color = TextLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Total faturado por categoria de produtos", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp))

                        // Category sums
                        val catSums = mutableMapOf("Bebidas" to 0.0, "Cabelo e barba" to 0.0, "Perfumes" to 0.0)
                        appointments.filter { it.timestamp in monthStart..monthEnd }.forEach { app ->
                            if (app.productsJson.isNotBlank()) {
                                try {
                                    val prodArray = JSONArray(app.productsJson)
                                    for (j in 0 until prodArray.length()) {
                                        val pObj = prodArray.getJSONObject(j)
                                        val category = pObj.optString("category", "Outros")
                                        val price = pObj.optDouble("price", 0.0)
                                        val qty = pObj.optInt("quantity", 1)
                                        catSums[category] = (catSums[category] ?: 0.0) + (price * qty)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        val maxCatVal = catSums.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            catSums.forEach { (cat, total) ->
                                val pct = (total / maxCatVal).toFloat()
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(cat, color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(total.toCurrency(), color = BrightTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color(0xFF334155))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(pct)
                                                .fillMaxHeight()
                                                .background(BrightTeal)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Subordinate Progress Bars
@Composable
fun GoalProgressBar(title: String, score: Double, target: Double, accentColor: Color) {
    val pct = (score / target).toFloat().coerceIn(0f, 1f)
    val pctString = String.format(Locale.getDefault(), "%.1f%%", (score / target) * 100)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (score >= target) Icons.Filled.CheckCircle else Icons.Filled.Flag,
                    contentDescription = null,
                    tint = if (score >= target) BrightTeal else TextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, color = TextLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Text(
                "${score.toCurrency()} / ${target.toCurrency()} ($pctString)",
                color = if (score >= target) BrightTeal else TextLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .background(accentColor)
            )
        }
    }
}

data class IndicatorData(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val iconColor: Color
)
