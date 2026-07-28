package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnerDao {
    @Query("SELECT * FROM owner_config LIMIT 1")
    suspend fun getOwnerConfig(): OwnerConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnerConfig(config: OwnerConfig)
}

@Dao
interface ProfessionalDao {
    @Query("SELECT * FROM professionals ORDER BY name ASC")
    fun getAllProfessionalsFlow(): Flow<List<Professional>>

    @Query("SELECT * FROM professionals ORDER BY name ASC")
    suspend fun getAllProfessionals(): List<Professional>

    @Query("SELECT * FROM professionals WHERE id = :id")
    suspend fun getProfessionalById(id: Int): Professional?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessional(professional: Professional)

    @Update
    suspend fun updateProfessional(professional: Professional)

    @Delete
    suspend fun deleteProfessional(professional: Professional)
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClientsFlow(): Flow<List<Client>>

    @Query("SELECT * FROM clients ORDER BY name ASC")
    suspend fun getAllClients(): List<Client>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: Int): Client?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY name ASC")
    fun getAllServicesFlow(): Flow<List<Service>>

    @Query("SELECT * FROM services ORDER BY name ASC")
    suspend fun getAllServices(): List<Service>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getServiceById(id: Int): Service?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: Service)

    @Update
    suspend fun updateService(service: Service)

    @Delete
    suspend fun deleteService(service: Service)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProducts(): List<Product>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY timestamp DESC")
    fun getAllAppointmentsFlow(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments ORDER BY timestamp DESC")
    suspend fun getAllAppointments(): List<Appointment>

    @Query("SELECT * FROM appointments WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    suspend fun getAppointmentsInPeriod(start: Long, end: Long): List<Appointment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE timestamp >= :start AND timestamp <= :end ORDER BY timestamp DESC")
    suspend fun getExpensesInPeriod(start: Long, end: Long): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}

@Dao
interface BillingGoalDao {
    @Query("SELECT * FROM billing_goals WHERE monthYear = :monthYear LIMIT 1")
    suspend fun getGoalByMonth(monthYear: String): BillingGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: BillingGoal)
}
