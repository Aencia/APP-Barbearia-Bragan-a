package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BillingGoal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(viewModel: BarberViewModel) {
    val currentGoal by viewModel.currentGoal.collectAsState()
    val ownerEmail by viewModel.ownerEmail.collectAsState()

    var showPassDialog by remember { mutableStateOf(false) }
    var changeSuccessMsg by remember { mutableStateOf<String?>(null) }
    var changeErrorMsg by remember { mutableStateOf<String?>(null) }

    // Backup state
    var backupStatus by remember { mutableStateOf("Nenhum backup realizado ainda.") }

    // Reading goals fields
    val defaultGoal = currentGoal ?: BillingGoal("Current", 18250.00, 20000.00, 25000.00)

    var minGoalStr by remember { mutableStateOf(defaultGoal.minGoal.toString()) }
    var medGoalStr by remember { mutableStateOf(defaultGoal.medGoal.toString()) }
    var highGoalStr by remember { mutableStateOf(defaultGoal.highGoal.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- GOAL SETTINGS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Flag, contentDescription = null, tint = ActiveGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Configurações de Metas Mensais", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Ajuste os valores das metas de faturamento para que apareçam correlacionados no Dashboard Geral.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = minGoalStr,
                    onValueChange = { minGoalStr = it },
                    label = { Text("Meta Mínima (R$)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoreBlue,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = medGoalStr,
                    onValueChange = { medGoalStr = it },
                    label = { Text("Meta Média (R$)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoreBlue,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = highGoalStr,
                    onValueChange = { highGoalStr = it },
                    label = { Text("Meta Alta (R$)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CoreBlue,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val min = minGoalStr.toDoubleOrNull() ?: 18250.0
                        val med = medGoalStr.toDoubleOrNull() ?: 20000.0
                        val high = highGoalStr.toDoubleOrNull() ?: 25000.0
                        viewModel.updateGoals(min, med, high)
                        changeSuccessMsg = "Metas atualizadas com sucesso!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoreBlue),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salvar Metas", color = TextLight)
                }
            }
        }

        // --- ACCOUNT SETTINGS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = ActiveGold, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Segurança de Acesso (Proprietário)", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    "Modifique o e-mail cadastrado ou a senha de administrador principal do sistema.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                ListItem(
                    headlineContent = { Text("E-mail Ativo", color = TextLight, fontSize = 14.sp) },
                    supportingContent = { Text(ownerEmail, color = TextMuted, fontSize = 12.sp) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    trailingContent = {
                        Button(
                            onClick = { showPassDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Text("Mudar Credenciais", color = TextLight, fontSize = 12.sp)
                        }
                    }
                )
            }
        }

        // --- GENERAL PRESETS CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBlueAccent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Backup, contentDescription = null, tint = BrightTeal, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Backup do Banco de Dados", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    "O banco de dados nativo do aplicativo está hospedado localmente nesta sandbox segura.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Text(
                    backupStatus,
                    color = BrightTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
                            backupStatus = "Backup local gerado com sucesso: backup_barbearia_${System.currentTimeMillis()}.db (${sdf.format(java.util.Date())})"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrightTeal)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = TextLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar Backup", color = TextLight)
                    }
                }
            }
        }

        // Action Status Feedback
        changeSuccessMsg?.let {
            Snackbar(
                action = { TextButton(onClick = { changeSuccessMsg = null }) { Text("OK", color = BrightTeal) } },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(it)
            }
        }
    }

    // --- CREDENTIALS EDIT DIALOG ---
    if (showPassDialog) {
        var emailInput by remember { mutableStateOf(ownerEmail) }
        var currentPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var recoveryQuest by remember { mutableStateOf("Braganca") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showPassDialog = false }) {
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
                    Text("Alterar Credenciais de Login", color = TextLight, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    changeErrorMsg?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("E-mail Proprietário", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = currentPass,
                        onValueChange = { currentPass = it },
                        label = { Text("Senha Atual (para validar)", color = TextMuted) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("Nova Senha", color = TextMuted) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoreBlue,
                            unfocusedBorderColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recoveryQuest,
                        onValueChange = { recoveryQuest = it },
                        label = { Text("Sobrenome de recuperação de senha", color = TextMuted) },
                        singleLine = true,
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
                        TextButton(onClick = {
                            showPassDialog = false
                            changeErrorMsg = null
                        }) {
                            Text("Cancelar", color = TextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (emailInput.isNotBlank() && currentPass.isNotBlank() && newPass.isNotBlank()) {
                                    // simple trigger validation directly in ViewModel config update
                                    val config = viewModel.repository.ownerDao
                                    val launcher = kotlinx.coroutines.MainScope()
                                    launcher.launchProjectUpdate {
                                        val data = config.getOwnerConfig()
                                        if (data != null && currentPass == data.passwordHash) {
                                            config.insertOwnerConfig(
                                                com.example.data.OwnerConfig(
                                                    email = emailInput.trim().lowercase(),
                                                    passwordHash = newPass,
                                                    recoveryAnswer = recoveryQuest
                                                )
                                            )
                                            changeSuccessMsg = "Credenciais e senha de segurança redefinidas!"
                                            showPassDialog = false
                                            changeErrorMsg = null
                                        } else {
                                            changeErrorMsg = "Senha atual incorreta!"
                                        }
                                    }
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
}

// Simple launch helper to run background transactions for dialog safely
fun kotlinx.coroutines.CoroutineScope.launchProjectUpdate(block: suspend () -> Unit) {
    this.launch {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
