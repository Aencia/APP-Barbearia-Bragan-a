package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owner_config")
data class OwnerConfig(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val recoveryAnswer: String
)

@Entity(tableName = "professionals")
data class Professional(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String,
    val serviceCommissionPercentage: Double,
    val productSalesGoal: Double,
    val active: Boolean = true
)

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val firstServiceDate: Long,
    val lastServiceDate: Long,
    val totalServices: Int = 0,
    val planName: String = "Nenhum",
    val planValue: Double = 0.0,
    val planActive: Boolean = false,
    val planStartDate: Long = 0L
)

@Entity(tableName = "services")
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val durationMinutes: Int,
    val commissionPercentage: Double,
    val active: Boolean = true
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val stock: Int,
    val cost: Double,
    val price: Double,
    val commissionPercentage: Double,
    val active: Boolean = true
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val clientName: String,
    val professionalId: Int,
    val professionalName: String,
    val timestamp: Long,
    val paymentMethod: String,
    val servicesJson: String, // JSON list of services performed
    val productsJson: String, // JSON list of products sold
    val totalValue: Double,
    val commissionValue: Double,
    val observations: String
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val description: String,
    val value: Double,
    val timestamp: Long,
    val isFixed: Boolean
)

@Entity(tableName = "billing_goals")
data class BillingGoal(
    @PrimaryKey val monthYear: String, // e.g., "06/2026"
    val minGoal: Double = 18250.00,
    val medGoal: Double = 20000.00,
    val highGoal: Double = 25000.00
)
