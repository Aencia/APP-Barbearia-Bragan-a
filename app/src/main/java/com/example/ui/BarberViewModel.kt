package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BarberViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = Repository(db)

    // UI States - Local and Flow based
    val professionals: StateFlow<List<Professional>> = repository.professionalsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clients: StateFlow<List<Client>> = repository.clientsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<Service>> = repository.servicesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.appointmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.expensesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication States
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _ownerEmail = MutableStateFlow("proprietario@braganca.com")
    val ownerEmail: StateFlow<String> = _ownerEmail.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _recoverySuccess = MutableStateFlow<String?>(null)
    val recoverySuccess: StateFlow<String?> = _recoverySuccess.asStateFlow()

    // Goals State
    private val _currentGoal = MutableStateFlow<BillingGoal?>(null)
    val currentGoal: StateFlow<BillingGoal?> = _currentGoal.asStateFlow()

    init {
        viewModelScope.launch {
            // Load config or pre-populate
            repository.ensureDefaultDataPopulated()
            loadCurrentGoals()
            checkSavedSession()
        }
    }

    private fun checkSavedSession() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("barber_session", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("logged_in_email", null)
        if (savedEmail != null) {
            _isLoggedIn.value = true
            _ownerEmail.value = savedEmail
        }
    }

    private suspend fun loadCurrentGoals() {
        val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        _currentGoal.value = repository.billingGoalDao.getGoalByMonth(monthYear)
    }

    // Login Method
    fun login(email: String, password: String): Boolean {
        var success = false
        viewModelScope.launch {
            val config = repository.ownerDao.getOwnerConfig()
            if (config != null && email.trim().lowercase() == config.email.lowercase() && password == config.passwordHash) {
                _isLoggedIn.value = true
                _ownerEmail.value = config.email
                _loginError.value = null
                // Save session
                val sharedPrefs = getApplication<Application>().getSharedPreferences("barber_session", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("logged_in_email", config.email).apply()
                success = true
            } else {
                _loginError.value = "Credenciais inválidas. Verifique seu e-mail e senha."
                success = false
            }
        }
        return success
    }

    // Logout Method
    fun logout() {
        _isLoggedIn.value = false
        _ownerEmail.value = ""
        val sharedPrefs = getApplication<Application>().getSharedPreferences("barber_session", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("logged_in_email").apply()
    }

    // Password Recovery
    fun recoverPassword(email: String, securityAnswer: String, newPass: String) {
        viewModelScope.launch {
            val config = repository.ownerDao.getOwnerConfig()
            if (config != null && email.trim().lowercase() == config.email.lowercase() &&
                securityAnswer.trim().lowercase() == config.recoveryAnswer.lowercase()
            ) {
                repository.ownerDao.insertOwnerConfig(config.copy(passwordHash = newPass))
                _recoverySuccess.value = "Senha alterada com sucesso! Faça login com a nova senha."
                _loginError.value = null
            } else {
                _loginError.value = "Resposta de recuperação de senha incorreta ou e-mail inválido."
                _recoverySuccess.value = null
            }
        }
    }

    fun clearAuthMessages() {
        _loginError.value = null
        _recoverySuccess.value = null
    }

    // --- GOALS CRUD ---
    fun updateGoals(min: Double, med: Double, high: Double) {
        viewModelScope.launch {
            val monthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
            val updated = BillingGoal(monthYear, minGoal = min, medGoal = med, highGoal = high)
            repository.billingGoalDao.insertGoal(updated)
            _currentGoal.value = updated
        }
    }

    // --- PROFESSIONALS CRUD ---
    fun addProfessional(name: String, role: String, commission: Double, goal: Double) {
        viewModelScope.launch {
            repository.professionalDao.insertProfessional(
                Professional(name = name, role = role, serviceCommissionPercentage = commission, productSalesGoal = goal)
            )
        }
    }

    fun updateProfessional(professional: Professional) {
        viewModelScope.launch {
            repository.professionalDao.updateProfessional(professional)
        }
    }

    fun deleteProfessional(professional: Professional) {
        viewModelScope.launch {
            repository.professionalDao.deleteProfessional(professional)
        }
    }

    // --- CLIENTS CRUD ---
    fun addClient(
        name: String,
        phone: String,
        planName: String = "Nenhum",
        planValue: Double = 0.0,
        planActive: Boolean = false,
        planStartDate: Long = 0L
    ) {
        viewModelScope.launch {
            repository.clientDao.insertClient(
                Client(
                    name = name,
                    phone = phone,
                    firstServiceDate = System.currentTimeMillis(),
                    lastServiceDate = System.currentTimeMillis(),
                    totalServices = 0,
                    planName = planName,
                    planValue = planValue,
                    planActive = planActive,
                    planStartDate = planStartDate
                )
            )
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            repository.clientDao.updateClient(client)
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.clientDao.deleteClient(client)
        }
    }

    // --- SERVICES CRUD ---
    fun addService(name: String, price: Double, duration: Int, commission: Double) {
        viewModelScope.launch {
            repository.serviceDao.insertService(
                Service(name = name, price = price, durationMinutes = duration, commissionPercentage = commission)
            )
        }
    }

    fun updateService(service: Service) {
        viewModelScope.launch {
            repository.serviceDao.updateService(service)
        }
    }

    fun deleteService(service: Service) {
        viewModelScope.launch {
            repository.serviceDao.deleteService(service)
        }
    }

    // --- PRODUCTS CRUD ---
    fun addProduct(name: String, category: String, stock: Int, cost: Double, price: Double, commission: Double) {
        viewModelScope.launch {
            repository.productDao.insertProduct(
                Product(name = name, category = category, stock = stock, cost = cost, price = price, commissionPercentage = commission)
            )
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.productDao.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.productDao.deleteProduct(product)
        }
    }

    // --- ATENDIMENTOS CRUD ---
    fun addAppointment(
        clientId: Int,
        clientName: String,
        profId: Int,
        profName: String,
        servicesSelected: List<Service>,
        productsSelected: List<Pair<Product, Int>>,
        paymentMethod: String,
        timestamp: Long,
        obs: String
    ) {
        viewModelScope.launch {
            // Count sums
            val servicesTotal = servicesSelected.sumOf { it.price }
            val servicesCommission = servicesSelected.sumOf { it.price * (it.commissionPercentage / 100.0) }

            val productsTotal = productsSelected.sumOf { it.first.price * it.second }
            val productsCommission = productsSelected.sumOf { (it.first.price * it.second) * (it.first.commissionPercentage / 100.0) }

            val totalVal = servicesTotal + productsTotal
            val commissionVal = servicesCommission + productsCommission

            // Json representation
            val servicesArray = JSONArray()
            servicesSelected.forEach {
                servicesArray.put(
                    com.example.ui.JSONObjectBuilder()
                        .put("id", it.id)
                        .put("name", it.name)
                        .put("price", it.price)
                        .build()
                )
            }

            val productsArray = JSONArray()
            productsSelected.forEach { (prod, qty) ->
                productsArray.put(
                    com.example.ui.JSONObjectBuilder()
                        .put("id", prod.id)
                        .put("name", prod.name)
                        .put("category", prod.category)
                        .put("quantity", qty)
                        .put("price", prod.price)
                        .build()
                )
            }

            val appt = Appointment(
                clientId = clientId,
                clientName = clientName,
                professionalId = profId,
                professionalName = profName,
                timestamp = timestamp,
                paymentMethod = paymentMethod,
                servicesJson = servicesArray.toString(),
                productsJson = productsArray.toString(),
                totalValue = totalVal,
                commissionValue = commissionVal,
                observations = obs
            )

            repository.createAppointment(appt)
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.appointmentDao.deleteAppointment(appointment)
        }
    }

    // --- EXPENSES CRUD ---
    fun addExpense(category: String, desc: String, value: Double, timestamp: Long, isFixed: Boolean) {
        viewModelScope.launch {
            repository.expenseDao.insertExpense(
                Expense(category = category, description = desc, value = value, timestamp = timestamp, isFixed = isFixed)
            )
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            repository.expenseDao.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.expenseDao.deleteExpense(expense)
        }
    }

}

// Helper builder to work gracefully with Json
class JSONObjectBuilder {
    private val map = mutableMapOf<String, Any>()
    fun put(key: String, value: Any): JSONObjectBuilder {
        map[key] = value
        return this
    }
    fun build(): org.json.JSONObject {
        return org.json.JSONObject(map)
    }
}
