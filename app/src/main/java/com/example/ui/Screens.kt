package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Navy Blue Palette Constants
val DeepNavy = Color(0xFF0F172A)     // Backgrounds / Sidebars
val DarkBlueAccent = Color(0xFF1E293B) // Card backgrounds / Elevated panels
val CoreBlue = Color(0xFF1D4ED8)       // Primary action navy blue
val BrightTeal = Color(0xFF0D9488)     // Positive trends
val WarmGold = Color(0xFFE2E8F0)       // Secondary text
val ActiveGold = Color(0xFFD97706)     // Alert / Pending / Accent Goals
val TextLight = Color(0xFFF8FAFC)      // High contrast text
val TextMuted = Color(0xFF94A3B8)      // Description text

// Helper for formatting Portuguese currency
fun Double.toCurrency(): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(this)
}

// Format Dates
fun Long.toDateString(pattern: String = "dd/MM/yyyy HH:mm"): String {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale("pt", "BR"))
        sdf.format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun MainLayout(viewModel: BarberViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    if (!isLoggedIn) {
        LoginScreen(viewModel)
    } else {
        AppNavigationContainer(viewModel)
    }
}

// --- LOGIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: BarberViewModel) {
    var email by remember { mutableStateOf("proprietario@braganca.com") }
    var password by remember { mutableStateOf("admin123") }
    var isRecoveryMode by remember { mutableStateOf(false) }
    var securityAnswer by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val loginError by viewModel.loginError.collectAsState()
    val recoverySuccess by viewModel.recoverySuccess.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepNavy, Color(0xFF050B14))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 450.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header / Brand Icon
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Barbearia Bragança",
                    color = TextLight,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isRecoveryMode) "Recuperação de Acesso" else "Painel de Gestão",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Error Feedback
                loginError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFF2D181A), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }

                // Success Feedback
                recoverySuccess?.let {
                    Text(
                        text = it,
                        color = BrightTeal,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color(0xFF142D26), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }

                // E-mail field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail do Proprietário", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoreBlue,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!isRecoveryMode) {
                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha", color = TextMuted) },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Show Password",
                                    tint = TextMuted
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.login(email, password) },
                        colors = ButtonDefaults.buttonColors(containerColor = CoreBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Acessar Painel", color = TextLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        isRecoveryMode = true
                        viewModel.clearAuthMessages()
                    }) {
                        Text("Esqueceu sua senha?", color = TextMuted, fontSize = 13.sp)
                    }
                } else {
                    // Security Question
                    Text(
                        text = "Pergunta de segurança: Qual o sobrenome fundador da barbearia? (Ex: Braganca)",
                        color = TextLight,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = securityAnswer,
                        onValueChange = { securityAnswer = it },
                        label = { Text("Resposta técnica de segurança", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nova Senha", color = TextMuted) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.recoverPassword(email, securityAnswer, newPassword)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ActiveGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Redefinir Senha", color = TextLight, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = {
                        isRecoveryMode = false
                        viewModel.clearAuthMessages()
                    }) {
                        Text("Voltar para login", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}


// --- NAVIGATION CONTAINER COMPONENT ---
// Combines Responsive Navigation Sidebar for large widths with Mobile Navigation Rails or bottom bars
@Composable
fun AppNavigationContainer(viewModel: BarberViewModel) {
    var selectedScreen by remember { mutableStateOf("dashboard") }

    // Navigation lists
    val navigationItems = listOf(
        NavigationItem("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        NavigationItem("clientes", "Clientes", Icons.Filled.Group, Icons.Outlined.Group),
        NavigationItem("profissionais", "Profissionais", Icons.Filled.ContentCut, Icons.Outlined.ContentCut),
        NavigationItem("servicos", "Serviços", Icons.Filled.Assignment, Icons.Outlined.Assignment),
        NavigationItem("produtos", "Produtos", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
        NavigationItem("atendimentos", "Atendimentos", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        NavigationItem("financeiro", "Financeiro", Icons.Filled.AttachMoney, Icons.Outlined.AttachMoney),
        NavigationItem("relatorios", "Relatórios", Icons.Filled.Assessment, Icons.Outlined.Assessment),
        NavigationItem("configuracoes", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 750.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar for Tablets / Large displays (Always show side navigation menu)
            if (isTablet) {
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                        .background(DeepNavy)
                        .padding(16.dp)
                ) {
                    // Header Brand
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "B. Bragança",
                            color = TextLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(bottom = 24.dp))

                    // Buttons
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(navigationItems) { item ->
                            val isSelected = selectedScreen == item.id
                            val containerColor = if (isSelected) Color(0x301D4ED8) else Color.Transparent
                            val contentColor = if (isSelected) TextLight else TextMuted

                            Row(
                                modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(containerColor)
                                        .clickable { selectedScreen = item.id }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) ActiveGold else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = item.label,
                                    color = contentColor,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Logged in owner state
                    HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "Owner",
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Proprietário", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Sessão Ativa", color = BrightTeal, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Sair", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Screen content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF0B0F19))
            ) {
                // Header (Top bar with title and mobile logout)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(DeepNavy)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isTablet) {
                        // Title for mobile
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Bragança",
                            color = TextLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Title for Tablet screen content
                        Text(
                            text = navigationItems.find { it.id == selectedScreen }?.label ?: "Barbearia",
                            color = TextLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!isTablet) {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Filled.Logout, contentDescription = "Sair", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Inner content display with transition anims
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = selectedScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                        },
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            "dashboard" -> DashboardScreen(viewModel)
                            "clientes" -> ClientesScreen(viewModel)
                            "profissionais" -> ProfissionaisScreen(viewModel)
                            "servicos" -> ServicosScreen(viewModel)
                            "produtos" -> ProdutosScreen(viewModel)
                            "atendimentos" -> AtendimentosScreen(viewModel)
                            "financeiro" -> FinanceiroScreen(viewModel)
                            "relatorios" -> RelatoriosScreen(viewModel)
                            "configuracoes" -> ConfiguracoesScreen(viewModel)
                        }
                    }
                }

                // Simple adaptive bottom navigation bar ONLY on smartphones
                if (!isTablet) {
                    NavigationBar(
                        containerColor = DeepNavy,
                        tonalElevation = 8.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        // To keep bar narrow, let's select 5 primary screens on mobile, and an "others" triggering a modal, or 5 primary tabs
                        val subsetMobileItems = listOf(
                            navigationItems[0], // Dashboard
                            navigationItems[1], // Clientes
                            navigationItems[5], // Atendimentos
                            navigationItems[6], // Financeiro
                            navigationItems[7], // Relatórios
                        )

                        subsetMobileItems.forEach { item ->
                            val isSelected = selectedScreen == item.id
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedScreen = item.id },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) ActiveGold else TextMuted
                                    )
                                },
                                label = { Text(item.label, fontSize = 10.sp, color = if (isSelected) TextLight else TextMuted) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = Color(0x301D4ED8)
                                )
                            )
                        }

                        // Config / Outros Item to trigger configuration screen easily on mobile
                        NavigationBarItem(
                            selected = selectedScreen == "configuracoes",
                            onClick = { selectedScreen = "configuracoes" },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.MoreHoriz,
                                    contentDescription = "Mais",
                                    tint = if (selectedScreen == "configuracoes") ActiveGold else TextMuted
                                )
                            },
                            label = { Text("Ajustes", fontSize = 10.sp, color = if (selectedScreen == "configuracoes") TextLight else TextMuted) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color(0x301D4ED8)
                            )
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val id: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
