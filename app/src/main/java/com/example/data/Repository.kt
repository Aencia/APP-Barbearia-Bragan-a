package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Repository(private val db: AppDatabase) {

    val ownerDao = db.ownerDao()
    val professionalDao = db.professionalDao()
    val clientDao = db.clientDao()
    val serviceDao = db.serviceDao()
    val productDao = db.productDao()
    val appointmentDao = db.appointmentDao()
    val expenseDao = db.expenseDao()
    val billingGoalDao = db.billingGoalDao()

    // Flow items
    val professionalsFlow: Flow<List<Professional>> = professionalDao.getAllProfessionalsFlow()
    val clientsFlow: Flow<List<Client>> = clientDao.getAllClientsFlow()
    val servicesFlow: Flow<List<Service>> = serviceDao.getAllServicesFlow()
    val productsFlow: Flow<List<Product>> = productDao.getAllProductsFlow()
    val appointmentsFlow: Flow<List<Appointment>> = appointmentDao.getAllAppointmentsFlow()
    val expensesFlow: Flow<List<Expense>> = expenseDao.getAllExpensesFlow()

    // Insert an appointment, update stock, and update client records automatically
    suspend fun createAppointment(appointment: Appointment) = withContext(Dispatchers.IO) {
        // 1. Insert appointment
        val appointmentId = appointmentDao.insertAppointment(appointment)

        // 2. Adjust client stats
        val client = clientDao.getClientById(appointment.clientId)
        if (client != null) {
            val updatedClient = client.copy(
                totalServices = client.totalServices + 1,
                lastServiceDate = appointment.timestamp,
                firstServiceDate = if (client.firstServiceDate == 0L) appointment.timestamp else client.firstServiceDate
            )
            clientDao.updateClient(updatedClient)
        }

        // 3. Adjust product stock
        if (appointment.productsJson.isNotBlank()) {
            try {
                val productsArray = JSONArray(appointment.productsJson)
                for (i in 0 until productsArray.length()) {
                    val pObj = productsArray.getJSONObject(i)
                    val pId = pObj.getInt("id")
                    val qty = pObj.optInt("quantity", 1)
                    val dbProduct = productDao.getProductById(pId)
                    if (dbProduct != null) {
                        val newStock = (dbProduct.stock - qty).coerceAtLeast(0)
                        productDao.updateProduct(dbProduct.copy(stock = newStock))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun ensureDefaultDataPopulated() = withContext(Dispatchers.IO) {
        try {
            // 1. Check Owner Config
            if (ownerDao.getOwnerConfig() == null) {
                ownerDao.insertOwnerConfig(
                    OwnerConfig(
                        email = "proprietario@braganca.com",
                        passwordHash = "admin123", // Plain text simplified for secure local-only setup
                        recoveryAnswer = "Braganca"
                    )
                )
            }

            // 2. Check Professionals
            if (professionalDao.getAllProfessionals().isEmpty()) {
                professionalDao.insertProfessional(
                    Professional(
                        name = "Gabriel Borges Grave Bragança",
                        role = "Fundador",
                        serviceCommissionPercentage = 45.0,
                        productSalesGoal = 500.0
                    )
                )
                professionalDao.insertProfessional(
                    Professional(
                        name = "Luccas",
                        role = "Barbeiro",
                        serviceCommissionPercentage = 45.0,
                        productSalesGoal = 500.0
                    )
                )
            }

            // 3. Check Services
            if (serviceDao.getAllServices().isEmpty()) {
                val initialServices = listOf(
                    Service(name = "Corte", price = 40.0, durationMinutes = 30, commissionPercentage = 45.0),
                    Service(name = "Barba", price = 40.0, durationMinutes = 25, commissionPercentage = 45.0),
                    Service(name = "Sobrancelha", price = 10.0, durationMinutes = 5, commissionPercentage = 45.0),
                    Service(name = "Pezinho", price = 20.0, durationMinutes = 10, commissionPercentage = 45.0),
                    Service(name = "Free Style", price = 5.0, durationMinutes = 5, commissionPercentage = 45.0),
                    Service(name = "Pigmentação", price = 30.0, durationMinutes = 20, commissionPercentage = 45.0),
                    Service(name = "Camuflagem", price = 40.0, durationMinutes = 20, commissionPercentage = 45.0),
                    Service(name = "Hidratação", price = 20.0, durationMinutes = 15, commissionPercentage = 45.0),
                    Service(name = "Depilação Nariz", price = 10.0, durationMinutes = 10, commissionPercentage = 45.0),
                    Service(name = "Depilação Sobrancelha", price = 10.0, durationMinutes = 10, commissionPercentage = 45.0),
                    Service(name = "Matização", price = 20.0, durationMinutes = 20, commissionPercentage = 45.0),
                    Service(name = "Luzes", price = 70.0, durationMinutes = 60, commissionPercentage = 45.0),
                    Service(name = "Nevou", price = 100.0, durationMinutes = 90, commissionPercentage = 45.0)
                )
                for (s in initialServices) {
                    serviceDao.insertService(s)
                }
            }

            // 4. Check Products
            if (productDao.getAllProducts().isEmpty()) {
                val initialProducts = listOf(
                    Product(name = "Cerveja Heineken", category = "Bebidas", stock = 60, cost = 6.00, price = 12.00, commissionPercentage = 10.0),
                    Product(name = "Refrigerante Lata", category = "Bebidas", stock = 45, cost = 2.50, price = 6.00, commissionPercentage = 5.0),
                    Product(name = "Pomada Modeladora Efeito Matte", category = "Cabelo e barba", stock = 22, cost = 15.00, price = 35.00, commissionPercentage = 10.0),
                    Product(name = "Óleo Hidratante para Barba", category = "Cabelo e barba", stock = 18, cost = 12.00, price = 30.00, commissionPercentage = 10.0),
                    Product(name = "Shampoo Anticaspa Forte", category = "Cabelo e barba", stock = 15, cost = 10.00, price = 25.00, commissionPercentage = 10.0),
                    Product(name = "Colônia Masculina Bragança", category = "Perfumes", stock = 8, cost = 35.00, price = 85.00, commissionPercentage = 15.0),
                    Product(name = "Pós Barba Importado", category = "Perfumes", stock = 5, cost = 40.00, price = 95.00, commissionPercentage = 15.0)
                )
                for (p in initialProducts) {
                    productDao.insertProduct(p)
                }
            }

            // 5. Check Expenses
            if (expenseDao.getAllExpenses().isEmpty()) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 5)
                val baseTime = calendar.timeInMillis
                val initialExpenses = listOf(
                    Expense(category = "Aluguel", description = "Aluguel do Ponto Comercial", value = 1311.00, timestamp = baseTime, isFixed = true),
                    Expense(category = "Energia", description = "Conta de Luz - Neoenergia", value = 800.00, timestamp = baseTime + 1000, isFixed = true),
                    Expense(category = "Salários", description = "Ajuda de custo fixo / Salários", value = 2000.00, timestamp = baseTime + 2000, isFixed = true),
                    Expense(category = "Contador", description = "Honorários Contábeis", value = 400.00, timestamp = baseTime + 3000, isFixed = true),
                    Expense(category = "Marketing", description = "Agência de Marketing/Social Media", value = 400.00, timestamp = baseTime + 4000, isFixed = true),
                    Expense(category = "Tráfego pago", description = "Anúncios Meta ADS + Google", value = 350.00, timestamp = baseTime + 5000, isFixed = true),
                    Expense(category = "Faxina", description = "Faxina quinzenal e limpeza", value = 200.00, timestamp = baseTime + 6000, isFixed = true),
                    Expense(category = "Ar-condicionado", description = "Manutenção preventiva ar", value = 200.00, timestamp = baseTime + 7000, isFixed = true),
                    Expense(category = "Cartões", description = "Taxas de máquina de cartão", value = 795.65, timestamp = baseTime + 8000, isFixed = true),
                    Expense(category = "BarberCode", description = "Assinatura Software de Gestão", value = 120.00, timestamp = baseTime + 9000, isFixed = true),
                    Expense(category = "Outros", description = "Despesas imprevistas fixas", value = 709.00, timestamp = baseTime + 10000, isFixed = true)
                )
                for (e in initialExpenses) {
                    expenseDao.insertExpense(e)
                }
            }

            // 6. Check Billing Goal (Current month)
            val currentMonthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
            if (billingGoalDao.getGoalByMonth(currentMonthYear) == null) {
                billingGoalDao.insertGoal(
                    BillingGoal(
                        monthYear = currentMonthYear,
                        minGoal = 18250.00,
                        medGoal = 20000.00,
                        highGoal = 25000.00
                    )
                )
            }

            // 7. Pre-populate Clients and Appointments to reflect the initial indicators
            if (clientDao.getAllClients().isEmpty()) {
                val clientsNames = listOf(
                    "Carlos Silva", "Andrei Souza", "Pedro Bragança", "João Oliveira", "Mateus Ferreira",
                    "Lucas Rezende", "Bruno Costa", "Rodrigo Nogueira", "Alexandre Pires", "Felipe Almeida",
                    "Rafael Santos", "Gustavo Lima", "Tiago Cardoso", "Vinicius Xavier", "Eduardo Ramos",
                    "Marcelo Dias", "Renato Vieira", "Daniel Castro", "Diego Mendes", "Juliano Alves",
                    "Guilherme Rocha", "Leandro Barbosa", "Vitor Antunes", "Leonardo Gomes", "Arthur Teixeira",
                    "Thiago Ribeiro", "Marcos Carvalho", "Caio Fonseca", "Fábio Martins", "Samuel Correia",
                    "Hugo Pinheiro", "Roberto Sobral", "Otávio Neves", "Denis Marques", "Douglas Prado"
                )

                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)

                // Let's seed clients
                val addedClients = mutableListOf<Client>()
                for ((index, name) in clientsNames.withIndex()) {
                    val phone = "(51) 9${(80000000 + index * 12345).toString().take(8)}"
                    calendar.set(currentYear, currentMonth, (1..25).random(), (9..19).random(), listOf(0, 15, 30, 45).random())
                    val apptTime = calendar.timeInMillis
                    val clientObj = Client(
                        name = name,
                        phone = phone,
                        firstServiceDate = apptTime - 15 * 24 * 60 * 60 * 1000L,
                        lastServiceDate = apptTime,
                        totalServices = 0
                    )
                    val cId = clientDao.insertClient(clientObj).toInt()
                    addedClients.add(clientObj.copy(id = cId))
                }

                // Seed 151 appointments
                val servicesList = serviceDao.getAllServices()
                val productsList = productDao.getAllProducts()
                val professionals = professionalDao.getAllProfessionals()

                if (addedClients.isNotEmpty() && servicesList.isNotEmpty() && professionals.isNotEmpty()) {
                    var accumulatedFaturamento = 0.0
                    val targetFaturamento = 10750.16
                    val totalAppointmentsNeeded = 151

                    val random = Random(42)
                    val paymentMethods = listOf("PIX", "Dinheiro", "Cartão de débito", "Cartão de crédito")

                    for (i in 0 until totalAppointmentsNeeded) {
                        val prof = professionals[i % professionals.size]
                        val clientIndex = if (i < 30) {
                            i % addedClients.size
                        } else {
                            val bound = addedClients.size.coerceAtMost(30)
                            if (bound > 0) random.nextInt(bound) else 0
                        }
                        val client = addedClients[clientIndex]

                        val service = servicesList[random.nextInt(servicesList.size)]
                        var totalVal = service.price
                        val serviceCommission = service.price * (service.commissionPercentage / 100.0)
                        var commissionVal = serviceCommission

                        val sArray = JSONArray()
                        val sObj = JSONObject().apply {
                            put("id", service.id)
                            put("name", service.name)
                            put("price", service.price)
                        }
                        sArray.put(sObj)

                        val pArray = JSONArray()
                        if (productsList.isNotEmpty() && random.nextDouble() < 0.4) {
                            val prod = productsList[random.nextInt(productsList.size)]
                            totalVal += prod.price
                            commissionVal += prod.price * (prod.commissionPercentage / 100.0)
                            val pObj = JSONObject().apply {
                                put("id", prod.id)
                                put("name", prod.name)
                                put("category", prod.category)
                                put("quantity", 1)
                                put("price", prod.price)
                            }
                            pArray.put(pObj)
                        }

                        if (i == totalAppointmentsNeeded - 1) {
                            val remaining = targetFaturamento - accumulatedFaturamento
                            if (remaining > 10) {
                                totalVal = remaining
                                commissionVal = remaining * 0.45
                            }
                        }

                        accumulatedFaturamento += totalVal

                        val apptCalendar = Calendar.getInstance()
                        apptCalendar.set(Calendar.DAY_OF_MONTH, (1..20).random())
                        apptCalendar.set(Calendar.HOUR_OF_DAY, (8..20).random())
                        apptCalendar.set(Calendar.MINUTE, listOf(0, 15, 30, 45).random())

                        val appointment = Appointment(
                            clientId = client.id,
                            clientName = client.name,
                            professionalId = prof.id,
                            professionalName = prof.name,
                            timestamp = apptCalendar.timeInMillis,
                            paymentMethod = paymentMethods.random(),
                            servicesJson = sArray.toString(),
                            productsJson = pArray.toString(),
                            totalValue = totalVal,
                            commissionValue = commissionVal,
                            observations = "Importado do histórico inicial"
                        )

                        appointmentDao.insertAppointment(appointment)

                        val currentClient = clientDao.getClientById(client.id)
                        if (currentClient != null) {
                            clientDao.updateClient(
                                currentClient.copy(
                                    totalServices = currentClient.totalServices + 1,
                                    lastServiceDate = appointment.timestamp,
                                    firstServiceDate = if (currentClient.firstServiceDate == 0L) appointment.timestamp else currentClient.firstServiceDate
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
